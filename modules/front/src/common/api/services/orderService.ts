import { createResourceService } from "../module/service/resourceFactory";
import type {
  OrderDTO,
  CreateOrderDTO,
  StatutCommandeType,
  OrderListResponseDTO,
  CommandeListDTO,
  CommandeUtilisateurDTO,
  ValidationFacturesClientResponseDTO,
} from "../types";
import { apiClient } from "./apiClient";

// Service de base avec méthodes CRUD standard
export const orderBaseService = createResourceService<OrderDTO>(
  apiClient,
  "/commandes",
);

// Service étendu avec méthodes spécifiques pour commandes
export const orderService = {
  ...orderBaseService,

  // Créer une nouvelle commande
  create: (order: CreateOrderDTO) =>
    apiClient.post<OrderDTO>("/commandes", {
      body: order,
    }),

  // Récupérer toutes les commandes de l'utilisateur connecté
  getMyOrders: () => apiClient.get<OrderDTO[]>("/commandes/me"),

  // Récupérer les commandes d'un utilisateur spécifique
  getByUtilisateur: (utilisateurUuid: string) =>
    apiClient.get<OrderDTO[]>(`/commandes/utilisateur/${utilisateurUuid}`),

  // Récupérer les commandes d'un deal
  getByDeal: (dealUuid: string) =>
    apiClient.get<OrderDTO[]>(`/commandes/deal/${dealUuid}`),

  // Récupérer les commandes par statut
  getByStatut: (statut: StatutCommandeType) =>
    apiClient.get<OrderDTO[]>(`/commandes/statut/${statut}`),

  // Mettre à jour le statut d'une commande
  updateStatus: (uuid: string, statut: StatutCommandeType) =>
    apiClient.patch<OrderDTO>(`/commandes/${uuid}/statut`, {
      body: { statut },
    }),

  // Annuler une commande
  cancel: (uuid: string) =>
    apiClient.patch<OrderDTO>(`/commandes/${uuid}/cancel`, {
      body: {},
    }),

  // Marquer une commande comme livrée
  markAsDelivered: (uuid: string) =>
    apiClient.patch<OrderDTO>(`/commandes/${uuid}/delivered`, {
      body: {},
    }),
};

// Service admin pour la gestion complète des commandes
export const orderAdminService = {
  // Lister toutes les commandes avec statistiques
  listAll: () => apiClient.get<OrderListResponseDTO>("/admin/commandes"),

  // Lister les commandes d'un marchand avec statistiques
  listByMerchant: (marchandUuid: string) =>
    apiClient.get<OrderListResponseDTO>(
      `/admin/commandes/marchand/${marchandUuid}`,
    ),

  // Récupérer les commandes par statut (admin)
  getByStatut: (statut: StatutCommandeType) =>
    apiClient.get<OrderDTO[]>(`/admin/commandes/statut/${statut}`),

  // Récupérer les commandes par utilisateur (admin)
  getByUtilisateur: (utilisateurUuid: string) =>
    apiClient.get<OrderDTO[]>(
      `/admin/commandes/utilisateur/${utilisateurUuid}`,
    ),

  // Récupérer les commandes par deal (admin)
  getByDeal: (dealUuid: string) =>
    apiClient.get<OrderDTO[]>(`/admin/commandes/deal/${dealUuid}`),

  // Mettre à jour le statut d'une commande (admin)
  updateStatus: (uuid: string, statut: StatutCommandeType) =>
    apiClient.patch<OrderDTO>(`/admin/commandes/${uuid}/statut`, {
      body: { statut },
    }),

  // Annuler une commande (admin)
  cancel: (uuid: string, raison?: string) =>
    apiClient.patch<OrderDTO>(`/admin/commandes/${uuid}/cancel`, {
      body: { raison },
    }),

  // Supprimer une commande (admin)
  delete: (uuid: string) => apiClient.delete<void>(`/admin/commandes/${uuid}`),

  // ✅ Valider le payout (admin) - retourne CommandeListDTO
  validatePayout: (uuid: string, dateDepotPayout: string) =>
    apiClient.post<CommandeListDTO>(`/admin/commandes/${uuid}/payout/valider`, {
      body: { dateDepotPayout },
    }),

  // ✅ Uploader la facture du vendeur - form field doit être "facture" (backend @RequestParam("facture"))
  // Retourne CommandeListDTO
  uploadSellerInvoice: (uuid: string, file: File) => {
    const formData = new FormData();
    formData.append("facture", file); // ✅ "facture" correspond au @RequestParam("facture") côté backend

    return apiClient.post<CommandeListDTO>(
      `/admin/commandes/${uuid}/facture/upload`,
        {
            body: formData,
        },
    );
  },

  // ✅ Valider les factures clients - payload { utilisateurUuids: [...] } + retourne ValidationFacturesClientResponseDTO
  validateCustomerInvoices: (
    uuid: string,
    utilisateurUuids: string[],
  ) =>
    apiClient.post<ValidationFacturesClientResponseDTO>(
      `/admin/commandes/${uuid}/factures/valider`,
      {
        body: { utilisateurUuids },
      },
    ),

  // ✅ Récupérer les clients d'une commande pour validation
  getOrderCustomers: (uuid: string) =>
    apiClient.get<CommandeUtilisateurDTO[]>(
      `/admin/commandes/user/command/${uuid}`,
    ),
};

// ⚠️ Déprécié : utiliser CommandeUtilisateurDTO (importé depuis types/order.ts)
/** @deprecated Utiliser CommandeUtilisateurDTO à la place */
export type Customer = CommandeUtilisateurDTO;

