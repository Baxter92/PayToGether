// hooks/useCategories.ts
import { categorieService } from "../services";
import type {
  CategoryDTO,
  CreateCategoryDTO,
  UpdateCategoryDTO,
} from "../types";
import { createResourceHooks } from "./factories/createResourceHooks";
import { useQuery } from "@tanstack/react-query";

// Créer les hooks de base
const categorieHooks = createResourceHooks<
  CategoryDTO,
  CreateCategoryDTO,
  UpdateCategoryDTO
>({
  resourceName: "categories",
  service: categorieService,
  customKeys: {
    byNom: (nom: string) => ["categories", "nom", nom] as const,
    exists: (nom: string) => ["categories", "exists", nom] as const,
  },
});

// Exporter les hooks de base
export const {
  keys: categorieKeys,
  useList: useCategories,
  useDetail: useCategorie,
  useCreate: useCreateCategorie,
  useUpdate: useUpdateCategorie,
  useDelete: useDeleteCategorie,
} = categorieHooks;

// Ajouter des hooks personnalisés
export const useCategorieByNom = (nom: string) => {
  return useQuery<CategoryDTO, Error>({
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
