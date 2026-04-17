/**
 * Types pour la pagination côté frontend
 */

export interface PaginationParams {
  page: number;
  size: number;
  [key: string]: string | number | boolean | null | undefined;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export const DEFAULT_PAGE_SIZE = 8;
export const DEFAULT_PAGE = 0;

export const PAGINATION_DEFAULTS = {
  PAGE: 0,
  SIZE: 10,
  SIZES: [5, 10, 20, 50, 100],
};

