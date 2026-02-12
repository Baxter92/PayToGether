// Permet d'adapter l'extraction des données au format de ton API.
import type { PaginatedResponse, PageMeta, CursorMeta } from "./types";

export type RawResponse = any;

export interface ResponseHandlerConfig {
  extractData?: (raw: RawResponse) => any;
  extractPaginated?: (raw: RawResponse) => PaginatedResponse<any> | null;
  extractCursor?: (raw: RawResponse) => PaginatedResponse<any> | null;
}

export class ResponseHandler {
  private cfg: ResponseHandlerConfig;

  constructor(cfg?: ResponseHandlerConfig) {
    this.cfg = cfg ?? {};
  }

  extractData<T>(raw: RawResponse): T {
    if (this.cfg.extractData) return this.cfg.extractData(raw) as T;
    if (raw?.data !== undefined) return raw.data;
    return raw;
  }

  extractPaginated<T>(raw: RawResponse): PaginatedResponse<T> | null {
    if (this.cfg.extractPaginated)
      return this.cfg.extractPaginated(raw) as PaginatedResponse<T>;
    if (raw?.data && raw?.meta)
      return { items: raw.data as T[], meta: raw.meta as PageMeta };
    if (raw?.items) {
      const meta: PageMeta = {
        page: raw.page ?? 1,
        limit: raw.limit ?? raw.items.length,
        total: raw.total,
        totalPages:
          raw.totalPages ??
          (raw.total && raw.limit
            ? Math.ceil(raw.total / raw.limit)
            : undefined),
      };
      return { items: raw.items as T[], meta };
    }
    return null;
  }

  extractCursor<T>(raw: RawResponse): PaginatedResponse<T> | null {
    if (this.cfg.extractCursor)
      return this.cfg.extractCursor(raw) as PaginatedResponse<T>;
    const items = raw?.data ?? raw?.items;
    const nextCursor = raw?.nextCursor ?? raw?.cursor ?? null;
    if (items)
      return { items: items as T[], meta: { nextCursor } as CursorMeta };
    return null;
  }
}
