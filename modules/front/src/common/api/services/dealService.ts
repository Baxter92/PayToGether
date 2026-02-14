import { createResourceService } from "../module/service/resourceFactory";
import type { CreateDealDTO, DealDTO, StatutDeal } from "../types";
import { apiClient } from "./apiClient";

// Service de base avec méthodes CRUD standard
export const dealBaseService = createResourceService<DealDTO>(
  apiClient,
  "/deals",
);

// Service étendu avec méthodes spécifiques
export const dealService = {
  ...dealBaseService,

  create: (deal: CreateDealDTO) =>
    apiClient.post<DealDTO>("/deals", {
      body: deal,
    }),

  // Récupérer les deals par statut
  getByStatut: (statut: typeof StatutDeal) =>
    apiClient.get<DealDTO[]>(`/deals/statut/${statut}`),

  // Récupérer les deals par créateur
  getByCreateur: (createurUuid: string) =>
    apiClient.get<DealDTO[]>(`/deals/createur/${createurUuid}`),

  // Récupérer les deals par catégorie
  getByCategorie: (categorieUuid: string) =>
    apiClient.get<DealDTO[]>(`/deals/categorie/${categorieUuid}`),

  // Confirmer l'upload d'une image
  confirmImageUpload: (dealUuid: string, imageUuid: string) =>
    apiClient.patch<void>(`/deals/${dealUuid}/images/${imageUuid}/confirm`),

  // Obtenir l'URL de lecture d'une image
  getImageUrl: (dealUuid: string, imageUuid: string) =>
    apiClient.get<{ url: string }>(
      `/deals/${dealUuid}/images/${imageUuid}/url`,
    ),
};
