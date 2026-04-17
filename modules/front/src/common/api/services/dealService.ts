import { createResourceService } from "../module/service/resourceFactory";
import type {
  CreateDealDTO,
  DealDTO,
  MyPaymentsDTO,
  StatutDealType,
} from "../types";
import type { PageResponse, PaginationParams } from "@/common/types/pagination";
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

  // Récupérer tous les deals (non paginé - pour rétrocompatibilité)
  getAll: () => apiClient.get<DealDTO[]>("/deals"),

  // Récupérer tous les deals avec pagination
  getAllPaginated: (params: PaginationParams) =>
    apiClient.get<PageResponse<DealDTO>>("/deals", { queryParams: params }),

  // Récupérer les deals par statut (non paginé - pour rétrocompatibilité)
  getByStatut: (statut: StatutDealType) =>
    apiClient.get<DealDTO[]>(`/deals/statut/${statut}`),

  // Récupérer les deals par statut avec pagination
  getByStatutPaginated: (statut: StatutDealType, params: PaginationParams) =>
    apiClient.get<PageResponse<DealDTO>>(`/deals/statut/${statut}`, {
      queryParams: params
    }),

  // Récupérer les deals par créateur (non paginé - pour rétrocompatibilité)
  getByCreateur: (createurUuid: string) =>
    apiClient.get<DealDTO[]>(`/deals/createur/${createurUuid}`),

  // Récupérer les deals par créateur avec pagination
  getByCreateurPaginated: (createurUuid: string, params: PaginationParams) =>
    apiClient.get<PageResponse<DealDTO>>(`/deals/createur/${createurUuid}`, {
      queryParams: params,
    }),

  // Récupérer les deals par catégorie (non paginé - pour rétrocompatibilité)
  getByCategorie: (categorieUuid: string) =>
    apiClient.get<DealDTO[]>(`/deals/categorie/${categorieUuid}`),

  // Récupérer les deals par catégorie avec pagination
  getByCategoriePaginated: (categorieUuid: string, params: PaginationParams) =>
    apiClient.get<PageResponse<DealDTO>>(`/deals/categorie/${categorieUuid}`, {
      queryParams: params,
    }),

  // Confirmer l'upload d'une image
  confirmImageUpload: (dealUuid: string, imageUuid: string) =>
    apiClient.patch<void>(`/deals/${dealUuid}/images/${imageUuid}/confirm`),

  // Obtenir l'URL de lecture d'une image
  getImageUrl: (dealUuid: string, imageUuid: string) =>
    apiClient.get<{ url: string }>(
      `/deals/${dealUuid}/images/${imageUuid}/url`,
    ),

  // Récupérer les villes disponibles
  getVilles: () => apiClient.get<string[]>("/deals/villes"),

  // Mettre à jour le statut d'un deal
  updateStatus: (uuid: string, statut: string) =>
    apiClient.patch<DealDTO>(`/deals/${uuid}/statut`, {
      body: { statut },
    }),

  // Mettre à jour les images d'un deal
  updateImages: (uuid: string, images: any) =>
    apiClient.patch<DealDTO>(`/deals/${uuid}/images`, {
      body: images,
    }),

  getParticipants: (dealUuid: string) =>
    apiClient.get<any[]>(`/deals/${dealUuid}/participants`),

  myPayments: () => apiClient.get<MyPaymentsDTO[]>("/deals/mes-paiements"),

  toggleFavoris: (uuid: string) =>
    apiClient.patch<DealDTO>(`/deals/${uuid}/favoris`),
};
