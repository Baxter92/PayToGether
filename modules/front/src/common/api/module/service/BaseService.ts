import type { ApiClient } from "../../client/ApiClient";
import type { RequestConfig } from "../../client/types";

export class BaseService<T> {
  protected client: ApiClient;
  protected resource: string;

  constructor(client: ApiClient, resource: string) {
    this.client = client;
    this.resource = resource;
  }

  list(params?: Record<string, any>, cfg?: RequestConfig): Promise<T[]> {
    return this.client.get<T[]>(this.resource, { queryParams: params, ...cfg });
  }

  getById(id: string, cfg?: RequestConfig): Promise<T> {
    return this.client.get<T>(`${this.resource}/${id}`, cfg);
  }

  create(payload: Partial<T>, cfg?: RequestConfig): Promise<T> {
    return this.client.post<T>(this.resource, { body: payload, ...cfg } as any);
  }

  update(id: string, payload: Partial<T>, cfg?: RequestConfig): Promise<T> {
    return this.client.put<T>(`${this.resource}/${id}`, {
      body: payload,
      ...cfg,
    } as any);
  }

  remove(id: string, cfg?: RequestConfig): Promise<void> {
    return this.client.delete<void>(`${this.resource}/${id}`, cfg);
  }
}
