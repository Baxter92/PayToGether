export * from "./category";
export * from "./deal";
export * from "./utilisateur";
export * from "./publicite";
export * from "./commentaire";
export * from "./payment";
export * from "./order";

export interface PaginatedResponse<T> {
  items: T[];
  meta: {
    page: number;
    limit: number;
    total?: number;
    nextCursor?: string | null;
  };
}
