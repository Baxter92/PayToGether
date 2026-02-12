// src/services/resourceFactory.ts
import type { ApiClient } from "../client/ApiClient";

export const createResourceService = <T>(
  client: ApiClient,
  resourcePath: string,
) => {
  return {
    list: (params?: Record<string, any>) =>
      client.get<T[]>(resourcePath, { queryParams: params }),
    getById: (id: string) => client.get<T>(`${resourcePath}/${id}`),
    create: (payload: Partial<T>) =>
      client.post<T>(resourcePath, { body: payload } as any),
    update: (id: string, payload: Partial<T>) =>
      client.put<T>(`${resourcePath}/${id}`, { body: payload } as any),
    remove: (id: string) => client.delete<void>(`${resourcePath}/${id}`),

    paginated: (opts?: {
      page?: number;
      limit?: number;
      query?: Record<string, any>;
    }) => client.getPaginated<T>(resourcePath, opts),

    cursorPage: (opts?: {
      cursor?: string | null;
      limit?: number;
      query?: Record<string, any>;
      cursorParamName?: string;
    }) => client.getCursorPage<T>(resourcePath, opts),
  };
};
