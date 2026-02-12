import { apiClient } from "../index";
import { createResourceService } from "../service/resourceFactory";
import type { Categorie } from "../types";

// Service de base avec méthodes CRUD standard
export const categorieBaseService = createResourceService<Categorie>(
  apiClient,
  "/categories"
);

// Service étendu avec méthodes spécifiques
export const categorieService = {
  ...categorieBaseService,

  // Récupérer une catégorie par nom
  getByNom: (nom: string) =>
    apiClient.get<Categorie>(`/categories/nom/${nom}`),

  // Vérifier si une catégorie existe par son nom
  existsByNom: (nom: string) =>
    apiClient.get<boolean>(`/categories/existe/${nom}`),
};