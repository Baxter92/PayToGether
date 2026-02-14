import axios from "axios";
import type {
  AxiosInstance,
  AxiosProgressEvent,
  AxiosRequestConfig,
  AxiosResponse,
} from "axios";
import type {
  RequestConfig,
  RequestContext,
  ApiPlugin,
  PaginatedResponse,
  ApiClientOptions,
} from "./types";
import { buildQueryString } from "./queryString";
import { HttpError } from "./HttpError";
import { ResponseHandler } from "./responseHandler";

export class ApiClient {
  private axios: AxiosInstance;
  private baseURL: string;
  private getToken?: () => string | null | Promise<string | null>;
  private defaultHeaders: Record<string, string>;
  private defaultRetry: number;
  private defaultTimeoutMs: number;
  private plugins: ApiPlugin[];
  private responseHandler: ResponseHandler;

  constructor(opts: ApiClientOptions) {
    this.baseURL = opts.baseURL.replace(/\/+$/, "");
    this.getToken = opts.getToken;
    this.defaultHeaders = opts.defaultHeaders ?? {
      "Content-Type": "application/json",
    };
    this.defaultRetry = opts.defaultRetry ?? 2;
    this.defaultTimeoutMs = opts.defaultTimeoutMs ?? 30_000;
    this.plugins = opts.plugins ?? [];
    this.responseHandler = opts.responseHandler ?? new ResponseHandler();

    this.axios = axios.create({
      baseURL: this.baseURL,
      timeout: this.defaultTimeoutMs,
      ...opts.axiosConfig,
    });

    // Optionnel: ajouter un interceptor pour uniformiser les erreurs axios -> HttpError (on laisse gestion principale dans requestRaw)
    this.axios.interceptors.response.use(
      (r) => r,
      (err) => {
        // axios err can be network/error/timeout
        return Promise.reject(err);
      },
    );
  }

  addPlugin(plugin: ApiPlugin) {
    this.plugins.push(plugin);
  }

  private async runPlugins<K extends keyof ApiPlugin>(
    hook: K,
    ...args: Parameters<NonNullable<ApiPlugin[K]>>
  ) {
    for (const plugin of this.plugins) {
      const fn = plugin[hook];
      if (!fn) continue;
      const result = await fn(...args);
      if (result !== undefined) return result;
    }
    return undefined;
  }

  private async buildHeaders(custom?: Record<string, string>) {
    const token = this.getToken ? await this.getToken() : null;
    const headers: Record<string, string> = {
      ...this.defaultHeaders,
      ...(custom ?? {}),
    };
    if (token) headers["Authorization"] = `Bearer ${token}`;
    return headers;
  }

  private buildURL(path: string, params?: Record<string, any>) {
    // if absolute URL, return it as-is (allow presigned urls)
    if (/^https?:\/\//i.test(path)) {
      const qs = params ? buildQueryString(params) : "";
      return `${path}${qs}`;
    }
    const trimmed = path.startsWith("/") ? path : `/${path}`;
    const qs = params ? buildQueryString(params) : "";
    return `${this.baseURL}${trimmed}${qs}`;
  }

  // Replay helper (public for refresh plugin)
  public async replay(ctx: RequestContext) {
    const path = ctx.url.startsWith(this.baseURL)
      ? ctx.url.slice(this.baseURL.length)
      : ctx.url;
    const config: RequestConfig = {
      headers: ctx.headers,
      body: ctx.body,
      signal: ctx.signal ?? undefined,
    };
    // use request (which applies responseHandler)
    return this.request(ctx.method as any, path, config);
  }

  // low-level using axios
  private async requestRaw(
    method: string,
    path: string,
    config?: RequestConfig,
  ) {
    const url = this.buildURL(path, config?.queryParams);

    const ctx: RequestContext = {
      url,
      method,
      headers: await this.buildHeaders(config?.headers),
      body: config?.body,
      signal: config?.signal ?? null,
      options: config,
    };

    // plugin onRequest: if a plugin returns a value we short-circuit and return it
    const onRequestResult = await this.runPlugins("onRequest", ctx);
    if (onRequestResult !== undefined) return onRequestResult;

    // axios request config
    const isAbsolute = /^https?:\/\//i.test(url);
    const axiosConfig: AxiosRequestConfig = {
      url: isAbsolute ? url : undefined,
      baseURL: isAbsolute ? undefined : this.baseURL,
      method: method as AxiosRequestConfig["method"],
      headers: ctx.headers,
      timeout: config?.timeoutMs ?? this.defaultTimeoutMs,
      signal: ctx.signal ?? undefined, // axios supports AbortController signal
      // params are already applied in buildURL, so we pass full url
    };

    // Data handling:
    // If body is File | Blob | FormData -> send raw (axios will handle)
    const isBinary =
      (typeof File !== "undefined" && ctx.body instanceof File) ||
      (typeof Blob !== "undefined" && ctx.body instanceof Blob) ||
      (typeof FormData !== "undefined" && ctx.body instanceof FormData);

    if (ctx.body !== undefined && ctx.body !== null) {
      if (isBinary) {
        axiosConfig.data = ctx.body;
        // For presigned PUT, often Content-Type must match: keep header if provided, otherwise don't set it
        // If header didn't include content-type and data is FormData, axios will set multipart boundary automatically.
      } else {
        // JSON payload (axios will stringify objects automatically if header is application/json)
        axiosConfig.data = ctx.body;
      }
    }

    // Support onUploadProgress from RequestConfig (browser only)
    if (config?.onUploadProgress) {
      axiosConfig.onUploadProgress = (progressEvent: AxiosProgressEvent) => {
        if (
          progressEvent.lengthComputable &&
          progressEvent.total !== undefined
        ) {
          const percent = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total,
          );
          config.onUploadProgress?.(percent);
        }
      };
    }

    const maxRetries = config?.retry ?? this.defaultRetry;
    let attempt = 0;

    while (true) {
      try {
        // If full url (absolute) we use axios.request with url set, else use path via baseURL
        const response: AxiosResponse = await this.axios.request(axiosConfig);
        // plugin onResponse
        const pluginResult = await this.runPlugins(
          "onResponse",
          response as any,
          ctx,
        );
        if (pluginResult !== undefined) return pluginResult;

        // status handling
        if (response.status === 204) return null;
        const contentType = (response.headers["content-type"] ?? "") as string;
        if (contentType.includes("application/json")) return response.data;
        return typeof response.data === "string"
          ? response.data
          : response.data;
      } catch (err: any) {
        // Normalize axios error
        // If axios error and has response -> convert to HttpError.fromResponse
        if (err?.response) {
          // build HttpError from response
          const httpErr = await HttpError.fromResponse({
            status: err.response.status,
            statusText: err.response.statusText,
            headers: err.response.headers,
            json: async () => err.response.data,
            text: async () =>
              typeof err.response.data === "string"
                ? err.response.data
                : JSON.stringify(err.response.data),
          } as any as Response);
          // plugin onError: allow plugin to handle and return a value
          const pluginResult = await this.runPlugins("onError", httpErr, ctx);
          if (pluginResult !== undefined) return pluginResult;
          // Decide retry logic for HTTP errors
          const status = httpErr.status;
          const networkLike = [408, 429, 502, 503, 504].includes(status!);
          const isAbort =
            err.name === "CanceledError" || err?.code === "ERR_CANCELED";
          const shouldRetry = !isAbort && attempt < maxRetries && networkLike;
          if (!shouldRetry) throw httpErr;
        } else {
          // network / timeout / cancellation
          const pluginResult = await this.runPlugins("onError", err, ctx);
          if (pluginResult !== undefined) return pluginResult;

          const isAbort =
            err?.name === "CanceledError" || err?.code === "ERR_CANCELED";
          const isTimeout =
            err?.code === "ECONNABORTED" ||
            err?.message ===
              "timeout of " +
                (config?.timeoutMs ?? this.defaultTimeoutMs) +
                "ms exceeded";
          const networkLike =
            err instanceof TypeError || isTimeout || !err?.response;
          const shouldRetry = !isAbort && attempt < maxRetries && networkLike;

          if (!shouldRetry) {
            // Normalize into HttpError
            throw new HttpError(0, null, err?.message ?? String(err));
          }
        }

        // backoff + jitter and retry
        attempt++;
        const baseDelay = Math.min(1000 * 2 ** attempt, 30_000);
        const jitter = Math.random() * 0.1 * baseDelay;
        const delay = baseDelay + jitter;
        await new Promise((res) => setTimeout(res, delay));
        continue;
      }
    }
  }

  // high-level typed request applying responseHandler.extractData
  private async request<T>(
    method: string,
    path: string,
    config?: RequestConfig,
  ): Promise<T> {
    const raw = await this.requestRaw(method, path, config);
    if (raw === null) return null as any;
    if (typeof raw === "string") return raw as any as T;
    return this.responseHandler.extractData<T>(raw);
  }

  // public convenience
  get<T>(path: string, config?: RequestConfig) {
    return this.request<T>("GET", path, config);
  }
  post<T>(path: string, config?: RequestConfig) {
    return this.request<T>("POST", path, config);
  }
  put<T>(path: string, config?: RequestConfig) {
    return this.request<T>("PUT", path, config);
  }
  patch<T>(path: string, config?: RequestConfig) {
    return this.request<T>("PATCH", path, config);
  }
  delete<T>(path: string, config?: RequestConfig) {
    return this.request<T>("DELETE", path, config);
  }

  // paginated (page/limit)
  async getPaginated<T>(
    path: string,
    opts?: { page?: number; limit?: number; query?: Record<string, any> },
  ): Promise<PaginatedResponse<T>> {
    const page = opts?.page ?? 1;
    const limit = opts?.limit ?? 20;
    const query = { ...(opts?.query ?? {}), page, limit };
    const raw = await this.requestRaw("GET", path, { queryParams: query });
    const parsed = this.responseHandler.extractPaginated<T>(raw);
    if (parsed) return parsed;
    const items = raw?.data ?? raw?.items ?? [];
    const meta = { page, limit, total: raw?.total } as any;
    return { items, meta };
  }

  // cursor-based (infinite)
  async getCursorPage<T>(
    path: string,
    opts?: {
      cursor?: string | null;
      limit?: number;
      query?: Record<string, any>;
      cursorParamName?: string;
    },
  ): Promise<PaginatedResponse<T>> {
    const cursorParamName = opts?.cursorParamName ?? "cursor";
    const query = {
      ...(opts?.query ?? {}),
      [cursorParamName]: opts?.cursor ?? null,
      limit: opts?.limit ?? 20,
    };
    const raw = await this.requestRaw("GET", path, { queryParams: query });
    const parsed = this.responseHandler.extractCursor<T>(raw);
    if (parsed) return parsed;
    const items = raw?.data ?? raw?.items ?? [];
    const meta = { nextCursor: raw?.nextCursor ?? null } as any;
    return { items, meta };
  }
}
