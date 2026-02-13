import { createResourceService } from "../module/service/resourceFactory";
import type { CategoryDTO } from "../types";
import { apiClient } from "./apiClient";

// Service de base avec méthodes CRUD standard
export const categorieBaseService = createResourceService<CategoryDTO>(
  apiClient,
  "/categories",
);

// Service étendu avec méthodes spécifiques
export const categorieService = {
  ...categorieBaseService,

  // Récupérer une catégorie par nom
  getByNom: (nom: string) =>
    apiClient.get<CategoryDTO>(`/categories/nom/${nom}`),

  // Vérifier si une catégorie existe par son nom
  existsByNom: (nom: string) =>
    apiClient.get<boolean>(`/categories/existe/${nom}`),
};
