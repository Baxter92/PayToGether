import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { categorieService } from "../services/categorieService";
import type { Categorie } from "../types";

// Clés de cache pour les requêtes
export const categorieKeys = {
  all: ["categories"] as const,
  lists: () => [...categorieKeys.all, "list"] as const,
  list: (filters: string) => [...categorieKeys.lists(), { filters }] as const,
  details: () => [...categorieKeys.all, "detail"] as const,
  detail: (id: string) => [...categorieKeys.details(), id] as const,
  byNom: (nom: string) => [...categorieKeys.all, "nom", nom] as const,
  exists: (nom: string) => [...categorieKeys.all, "exists", nom] as const,
};

// ===== QUERIES =====

export const useCategories = () => {
  return useQuery<Categorie[], Error>({
    queryKey: categorieKeys.lists(),
    queryFn: () => categorieService.list(),
  });
};

export const useCategorie = (uuid: string) => {
  return useQuery<Categorie, Error>({
    queryKey: categorieKeys.detail(uuid),
    queryFn: () => categorieService.getById(uuid),
    enabled: !!uuid,
  });
};

export const useCategorieByNom = (nom: string) => {
  return useQuery<Categorie, Error>({
    queryKey: categorieKeys.byNom(nom),
    queryFn: () => categorieService.getByNom(nom),
    enabled: !!nom,
  });
};

export const useCategorieExists = (nom: string) => {
  return useQuery<boolean, Error>({
    queryKey: categorieKeys.exists(nom),
    queryFn: () => categorieService.existsByNom(nom),
    enabled: !!nom,
  });
};

// ===== MUTATIONS =====

export const useCreateCategorie = () => {
  const queryClient = useQueryClient();

  return useMutation<
    Categorie,
    Error,
    Omit<Categorie, "uuid" | "dateCreation" | "dateModification">
  >({
    mutationFn: (data) => categorieService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categorieKeys.lists() });
    },
  });
};

export const useUpdateCategorie = () => {
  const queryClient = useQueryClient();

  return useMutation<
    Categorie,
    Error,
    { uuid: string; data: Partial<Categorie> }
  >({
    mutationFn: ({ uuid, data }) => categorieService.update(uuid, data),
    onSuccess: (updatedCategorie) => {
      queryClient.invalidateQueries({ queryKey: categorieKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: categorieKeys.detail(updatedCategorie.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: categorieKeys.byNom(updatedCategorie.nom),
      });
    },
  });
};

export const useDeleteCategorie = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (uuid) => categorieService.remove(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: categorieKeys.lists() });
    },
  });
};
