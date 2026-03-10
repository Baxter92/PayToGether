import { createResourceService } from "../module/service/resourceFactory";
import type {
  PaymentDTO,
  CreateSquarePaymentDTO,
  SquarePaymentResponseDTO,
  PaiementListResponseDTO,
} from "../types";
import { apiClient } from "./apiClient";

// Service de base avec méthodes CRUD standard
export const paymentBaseService = createResourceService<PaymentDTO>(
  apiClient,
  "/paiements",
);

// Service étendu avec méthodes spécifiques pour paiements
export const paymentService = {
  ...paymentBaseService,

  // Créer un paiement Square
  createSquarePayment: (payment: CreateSquarePaymentDTO) =>
    apiClient.post<SquarePaymentResponseDTO>("/square-payments", {
      body: payment,
    }),

  // Vérifier le statut d'un paiement Square
  checkSquarePaymentStatus: (paiementUuid: string) =>
    apiClient.get<SquarePaymentResponseDTO>(
      `/square-payments/${paiementUuid}/status`,
    ),

  // Rembourser un paiement Square
  refundSquarePayment: (paiementUuid: string) =>
    apiClient.post<SquarePaymentResponseDTO>(
      `/square-payments/${paiementUuid}/refund`,
      { body: {} },
    ),

  // Récupérer les paiements par statut
  getByStatut: (statut: string) =>
    apiClient.get<PaymentDTO[]>(`/paiements/statut/${statut}`),

  // Récupérer les paiements d'une commande
  getByCommande: (commandeUuid: string) =>
    apiClient.get<PaymentDTO[]>(`/paiements/commande/${commandeUuid}`),

  // Récupérer les paiements d'un utilisateur client
  getByClient: (clientUuid: string) =>
    apiClient.get<PaymentDTO[]>(`/paiements/client/${clientUuid}`),

  // Récupérer les paiements d'un marchand (vendeur)
  getByMarchand: (marchandUuid: string) =>
    apiClient.get<PaymentDTO[]>(`/paiements/marchand/${marchandUuid}`),
};

// Service admin pour les statistiques et gestion complète des paiements
export const paymentAdminService = {
  // Lister tous les paiements avec statistiques
  listAll: () => apiClient.get<PaiementListResponseDTO>("/admin/paiements"),

  // Récupérer les paiements par statut (admin)
  getByStatut: (statut: string) =>
    apiClient.get<PaymentDTO[]>(`/admin/paiements/statut/${statut}`),

  // Récupérer les paiements par méthode (admin)
  getByMethode: (methode: string) =>
    apiClient.get<PaymentDTO[]>(`/admin/paiements/methode/${methode}`),
};
