export * from "./category";
export * from "./deal";
export * from "./utilisateur";
// export * from "./publicite";

export interface PaginatedResponse<T> {
  items: T[];
  meta: {
    page: number;
    limit: number;
    total?: number;
    nextCursor?: string | null;
  };
}
