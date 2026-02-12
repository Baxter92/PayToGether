import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { dealService } from "../services/dealService";
import type { Deal } from "../types";

// Clés de cache pour les requêtes
export const dealKeys = {
  all: ["deals"] as const,
  lists: () => [...dealKeys.all, "list"] as const,
  list: (filters: string) => [...dealKeys.lists(), { filters }] as const,
  details: () => [...dealKeys.all, "detail"] as const,
  detail: (id: string) => [...dealKeys.details(), id] as const,
  byStatut: (statut: string) => [...dealKeys.all, "statut", statut] as const,
  byCreateur: (createurUuid: string) => [...dealKeys.all, "createur", createurUuid] as const,
  byCategorie: (categorieUuid: string) => [...dealKeys.all, "categorie", categorieUuid] as const,
};

// ===== QUERIES =====

export const useDeals = () => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.lists(),
    queryFn: () => dealService.list(),
  });
};

export const useDeal = (uuid: string) => {
  return useQuery<Deal, Error>({
    queryKey: dealKeys.detail(uuid),
    queryFn: () => dealService.getById(uuid),
    enabled: !!uuid,
  });
};

export const useDealsByStatut = (statut: string) => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.byStatut(statut),
    queryFn: () => dealService.getByStatut(statut),
    enabled: !!statut,
  });
};

export const useDealsByCreateur = (createurUuid: string) => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.byCreateur(createurUuid),
    queryFn: () => dealService.getByCreateur(createurUuid),
    enabled: !!createurUuid,
  });
};

export const useDealsByCategorie = (categorieUuid: string) => {
  return useQuery<Deal[], Error>({
    queryKey: dealKeys.byCategorie(categorieUuid),
    queryFn: () => dealService.getByCategorie(categorieUuid),
    enabled: !!categorieUuid,
  });
};

// ===== MUTATIONS =====

export const useCreateDeal = () => {
  const queryClient = useQueryClient();

  return useMutation<Deal, Error, Omit<Deal, "uuid" | "dateCreation" | "dateModification">>({
    mutationFn: (data) => dealService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
    },
  });
};

export const useUpdateDeal = () => {
  const queryClient = useQueryClient();

  return useMutation<Deal, Error, { uuid: string; data: Partial<Deal> }>({
    mutationFn: ({ uuid, data }) => dealService.update(uuid, data),
    onSuccess: (updatedDeal) => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
      queryClient.invalidateQueries({ queryKey: dealKeys.detail(updatedDeal.uuid) });
      queryClient.invalidateQueries({ queryKey: dealKeys.byStatut(updatedDeal.statut) });
      queryClient.invalidateQueries({ queryKey: dealKeys.byCreateur(updatedDeal.createurUuid) });
      queryClient.invalidateQueries({ queryKey: dealKeys.byCategorie(updatedDeal.categorieUuid) });
    },
  });
};

export const useDeleteDeal = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (uuid) => dealService.remove(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dealKeys.lists() });
    },
  });
};

export const useConfirmDealImageUpload = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, { dealUuid: string; imageUuid: string }>({
    mutationFn: ({ dealUuid, imageUuid }) => dealService.confirmImageUpload(dealUuid, imageUuid),
    onSuccess: (_, { dealUuid }) => {
      queryClient.invalidateQueries({ queryKey: dealKeys.detail(dealUuid) });
    },
  });
};

export const useGetDealImageUrl = (dealUuid: string, imageUuid: string) => {
  return useQuery<{ url: string }, Error>({
    queryKey: [...dealKeys.detail(dealUuid), "image-url", imageUuid],
    queryFn: () => dealService.getImageUrl(dealUuid, imageUuid),
    enabled: !!dealUuid && !!imageUuid,
  });
};