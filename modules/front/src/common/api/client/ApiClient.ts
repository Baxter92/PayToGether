// src/client/ApiClient.ts
import type {
  RequestConfig,
  RequestContext,
  ApiPlugin,
  PaginatedResponse,
} from "./types";
import { buildQueryString } from "./queryString";
import { HttpError } from "./HttpError";
import { ResponseHandler } from "./responseHandler";

export interface ApiClientOptions {
  baseURL: string;
  getToken?: () => string | null | Promise<string | null>;
  defaultHeaders?: Record<string, string>;
  defaultRetry?: number;
  defaultTimeoutMs?: number;
  plugins?: ApiPlugin[];
  responseHandler?: ResponseHandler;
}

export class ApiClient {
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
  }

  // Permet d'ajouter dynamiquement un plugin (utile pour le refresh plugin qui a besoin de replay())
  addPlugin(plugin: ApiPlugin) {
    this.plugins.push(plugin);
  }

  // runPlugins : exécute chaque plugin, si un plugin renvoie une valeur non-undefined, on la retourne
  private async runPlugins<K extends keyof ApiPlugin>(
    hook: K,
    ...args: Parameters<NonNullable<ApiPlugin[K]>>
  ) {
    for (const plugin of this.plugins) {
      const fn = plugin[hook];
      if (!fn) continue;
      // @ts-ignore
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
    const trimmed = path.startsWith("/") ? path : `/${path}`;
    const qs = params ? buildQueryString(params) : "";
    return `${this.baseURL}${trimmed}${qs}`;
  }

  private withTimeout<T>(p: Promise<T>, timeoutMs: number) {
    if (timeoutMs <= 0) return p;
    return Promise.race([
      p,
      new Promise<T>((_, reject) =>
        setTimeout(() => reject(new Error("Timeout")), timeoutMs),
      ),
    ]);
  }

  // --- Replay helper : ré-exécute la requête à partir d'un RequestContext
  // public pour que le refresh plugin puisse l'utiliser (via client.addPlugin)
  public async replay(ctx: RequestContext) {
    // compute relative path from full url
    const path = ctx.url.startsWith(this.baseURL)
      ? ctx.url.slice(this.baseURL.length)
      : ctx.url;
    // remove query string from path? no - buildURL will accept full path including query
    // call request with original method and options
    const config: RequestConfig = {
      headers: ctx.headers,
      body: ctx.body,
      signal: ctx.signal ?? undefined,
    };
    // method is included in ctx.method, but our request helper expects method+path+config.
    // Use lower-level requestRaw to preserve behaviour.
    return this.request(ctx.method as any, path, config);
  }

  // low-level that returns raw JSON/text and calls plugins
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

    // allow plugin to intercept request (and possibly return a value)
    const onRequestResult = await this.runPlugins("onRequest", ctx);
    if (onRequestResult !== undefined) return onRequestResult;

    const payload: RequestInit = {
      method,
      headers: ctx.headers,
      body:
        ctx.body !== undefined && ctx.body !== null
          ? JSON.stringify(ctx.body)
          : undefined,
      signal: ctx.signal ?? undefined,
    };

    const maxRetries = config?.retry ?? this.defaultRetry;
    let attempt = 0;

    while (true) {
      try {
        const raw = await this.withTimeout(
          fetch(url, payload),
          config?.timeoutMs ?? this.defaultTimeoutMs,
        );
        await this.runPlugins("onResponse", raw as Response, ctx);
        if (!raw.ok) throw await HttpError.fromResponse(raw as Response);
        if ((raw as Response).status === 204) return null;
        const contentType = (raw as Response).headers.get("content-type") || "";
        if (contentType.includes("application/json"))
          return await (raw as Response).json();
        return await (raw as Response).text();
      } catch (err: any) {
        // If plugin handles the error and returns a value (ex: refresh plugin replay), use it.
        const pluginResult = await this.runPlugins("onError", err, ctx);
        if (pluginResult !== undefined) return pluginResult;

        const isAbort = err instanceof Error && err.name === "AbortError";
        const isHttpErr = err instanceof HttpError;
        const status = isHttpErr ? err.status : null;

        // Decide if we should retry:
        const networkLike =
          err instanceof TypeError ||
          (isHttpErr && [408, 429, 502, 503, 504].includes(status!));
        const shouldRetry = !isAbort && attempt < maxRetries && networkLike;

        if (!shouldRetry) {
          // Normalize non-HttpError into HttpError wrapper
          if (err instanceof HttpError) throw err;
          throw new HttpError(0, null, err?.message ?? String(err));
        }

        // backoff + jitter
        attempt++;
        const baseDelay = Math.min(1000 * 2 ** attempt, 30_000);
        const jitter = Math.random() * 0.1 * baseDelay;
        const delay = baseDelay + jitter;
        await new Promise((res) => setTimeout(res, delay));
        continue; // retry
      }
    }
  }

  // high-level that returns typed data (applique ResponseHandler.extractData)
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

  // public convenience methods
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
