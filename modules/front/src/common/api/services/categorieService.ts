import { createResourceService } from "../module/service/resourceFactory";
import type { Categorie } from "../types";
import { apiClient } from "./apiClient";

// Service de base avec méthodes CRUD standard
export const categorieBaseService = createResourceService<Categorie>(
  apiClient,
  "/categories",
);

// Service étendu avec méthodes spécifiques
export const categorieService = {
  ...categorieBaseService,

  // Récupérer une catégorie par nom
  getByNom: (nom: string) => apiClient.get<Categorie>(`/categories/nom/${nom}`),

  // Vérifier si une catégorie existe par son nom
  existsByNom: (nom: string) =>
    apiClient.get<boolean>(`/categories/existe/${nom}`),
};
