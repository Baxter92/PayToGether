import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type {
  UseQueryOptions,
  UseMutationOptions,
} from "@tanstack/react-query";

export interface ResourceService<
  TEntity,
  TCreateDTO,
  TUpdateDTO = Partial<TEntity>,
> {
  list: (params?: any) => Promise<TEntity[]>;
  getById: (id: string) => Promise<TEntity>;
  create: (data: TCreateDTO) => Promise<TEntity>;
  update: (id: string, data: TUpdateDTO) => Promise<TEntity>;
  remove: (id: string) => Promise<void>;
}

export interface BaseResourceKeys {
  all: readonly string[];
  lists: () => readonly string[];
  list: (filters?: any) => readonly any[];
  details: () => readonly string[];
  detail: (id: string) => readonly any[];
}

export type ResourceKeys<T = any> = BaseResourceKeys & T;

export const createResourceKeys = <
  T extends Record<string, (...args: any[]) => readonly any[]> = any,
>(
  resourceName: string,
  customKeys?: T,
): ResourceKeys<T> => {
  const baseKeys: BaseResourceKeys = {
    all: [resourceName] as const,
    lists: function () {
      return [...this.all, "list"] as const;
    },
    list: function (filters?: any) {
      return [...this.lists(), { filters }] as const;
    },
    details: function () {
      return [...this.all, "detail"] as const;
    },
    detail: function (id: string) {
      return [...this.details(), id] as const;
    },
  };

  return { ...baseKeys, ...customKeys } as ResourceKeys<T>;
};

export interface CreateResourceHooksOptions<TEntity, TCreateDTO, TUpdateDTO> {
  resourceName: string;
  service: ResourceService<TEntity, TCreateDTO, TUpdateDTO>;
  customKeys?: Partial<ResourceKeys> &
    Record<string, (...args: any[]) => readonly any[]>;
}

export const createResourceHooks = <
  TEntity,
  TCreateDTO,
  TUpdateDTO = Partial<TEntity>,
>({
  resourceName,
  service,
  customKeys = {},
}: CreateResourceHooksOptions<TEntity, TCreateDTO, TUpdateDTO>) => {
  const keys = createResourceKeys(resourceName, customKeys);

  // ===== QUERIES =====

  const useList = (
    params?: any,
    options?: Omit<UseQueryOptions<TEntity[], Error>, "queryKey" | "queryFn">,
  ) => {
    return useQuery<TEntity[], Error>({
      queryKey: keys.list(params),
      queryFn: () => service.list(params),
      ...options,
    });
  };

  const useDetail = (
    id: string,
    options?: Omit<UseQueryOptions<TEntity, Error>, "queryKey" | "queryFn">,
  ) => {
    return useQuery<TEntity, Error>({
      queryKey: keys.detail(id),
      queryFn: () => service.getById(id),
      enabled: !!id,
      ...options,
    });
  };

  // ===== MUTATIONS =====

  const useCreate = (
    options?: Omit<
      UseMutationOptions<TEntity, Error, TCreateDTO>,
      "mutationFn"
    >,
  ) => {
    const queryClient = useQueryClient();

    return useMutation<TEntity, Error, TCreateDTO>({
      mutationFn: (data) => service.create(data),
      onSuccess: (data, variables, onMutateResult, context) => {
        queryClient.invalidateQueries({ queryKey: keys.lists() });
        options?.onSuccess?.(data, variables, onMutateResult, context);
      },
      ...options,
    });
  };

  const useUpdate = (
    options?: Omit<
      UseMutationOptions<TEntity, Error, { id: string; data: TUpdateDTO }>,
      "mutationFn"
    >,
  ) => {
    const queryClient = useQueryClient();

    return useMutation<TEntity, Error, { id: string; data: TUpdateDTO }>({
      mutationFn: ({ id, data }) => service.update(id, data),
      onSuccess: (data, variables, onMutateResult, context) => {
        queryClient.invalidateQueries({ queryKey: keys.lists() });
        queryClient.invalidateQueries({ queryKey: keys.detail(variables.id) });
        options?.onSuccess?.(data, variables, onMutateResult, context);
      },
      ...options,
    });
  };

  const useDelete = (
    options?: Omit<UseMutationOptions<void, Error, string>, "mutationFn">,
  ) => {
    const queryClient = useQueryClient();

    return useMutation<void, Error, string>({
      mutationFn: (id) => service.remove(id),
      onSuccess: (data, variables, onMutateResult, context) => {
        queryClient.invalidateQueries({ queryKey: keys.lists() });
        queryClient.invalidateQueries({ queryKey: keys.detail(variables) });
        options?.onSuccess?.(data, variables, onMutateResult, context);
      },
      ...options,
    });
  };

  return {
    keys,
    useList,
    useDetail,
    useCreate,
    useUpdate,
    useDelete,
  };
};
