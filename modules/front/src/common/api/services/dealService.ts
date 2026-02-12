import { createResourceService } from "../module/service/resourceFactory";
import type { Deal, StatutDeal } from "../types";
import { apiClient } from "./apiClient";

// Service de base avec méthodes CRUD standard
export const dealBaseService = createResourceService<Deal>(apiClient, "/deals");

// Service étendu avec méthodes spécifiques
export const dealService = {
  ...dealBaseService,

  create: (deal: Deal) =>
    apiClient.post<Deal>("/deals", {
      body: deal,
    }),

  // Récupérer les deals par statut
  getByStatut: (statut: typeof StatutDeal) =>
    apiClient.get<Deal[]>(`/deals/statut/${statut}`),

  // Récupérer les deals par créateur
  getByCreateur: (createurUuid: string) =>
    apiClient.get<Deal[]>(`/deals/createur/${createurUuid}`),

  // Récupérer les deals par catégorie
  getByCategorie: (categorieUuid: string) =>
    apiClient.get<Deal[]>(`/deals/categorie/${categorieUuid}`),

  // Confirmer l'upload d'une image
  confirmImageUpload: (dealUuid: string, imageUuid: string) =>
    apiClient.patch<void>(`/deals/${dealUuid}/images/${imageUuid}/confirm`),

  // Obtenir l'URL de lecture d'une image
  getImageUrl: (dealUuid: string, imageUuid: string) =>
    apiClient.get<{ url: string }>(
      `/deals/${dealUuid}/images/${imageUuid}/url`,
    ),
};
