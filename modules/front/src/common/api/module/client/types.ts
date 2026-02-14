// Types partagés pour ApiClient et plugins.

import type { AxiosRequestConfig } from "axios";
import type { ResponseHandler } from "./responseHandler";

export interface ApiClientOptions {
  baseURL: string;
  getToken?: () => string | null | Promise<string | null>;
  defaultHeaders?: Record<string, string>;
  defaultRetry?: number;
  defaultTimeoutMs?: number;
  plugins?: ApiPlugin[];
  responseHandler?: ResponseHandler;
  axiosConfig?: Partial<AxiosRequestConfig>;
}

export type QueryParams = Record<
  string,
  string | number | boolean | null | undefined
>;

export interface RequestConfig {
  headers?: Record<string, string>;
  queryParams?: QueryParams;
  body?: any;
  signal?: AbortSignal | null;
  timeoutMs?: number;
  retry?: number;
  onUploadProgress?: (percent: number) => void;
}

export interface RequestContext {
  url: string; // full url
  method: string;
  headers: Record<string, string>;
  body?: any;
  signal?: AbortSignal | null;
  options?: RequestConfig;
}

export interface ApiPlugin {
  /**
   * onRequest peut muter ctx (headers, body...)
   * Si renvoie une valeur non-undefined (Promise ou direct), cette valeur sera utilisée par ApiClient
   */
  onRequest?: (ctx: RequestContext) => Promise<any> | any;
  onResponse?: (
    res: Response,
    ctx: RequestContext,
  ) => Promise<Response> | Response | any;
  /**
   * onError peut retourner une valeur (par ex: replay d'une requête après refresh)
   * Si renvoie undefined -> continue la logique d'erreur normale.
   */
  onError?: (error: any, ctx: RequestContext) => Promise<any> | any;
}

export interface PageMeta {
  page: number;
  limit: number;
  total?: number;
  totalPages?: number;
}

export interface CursorMeta {
  nextCursor?: string | null;
  prevCursor?: string | null;
}

export interface PaginatedResponse<T> {
  items: T[];
  meta: PageMeta | CursorMeta;
}

export interface RefreshTokenConfig {
  enabled: boolean;

  refreshEndpoint: string;

  getRefreshToken?: () => Promise<string | null> | string | null | undefined;

  saveTokens: (
    accessToken: string,
    refreshToken?: string,
  ) => Promise<void> | void;

  onRefreshFailure?: (error: any) => void;
}
