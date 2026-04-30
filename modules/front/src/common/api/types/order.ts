import type { PaymentDTO } from "./payment";

// ✅ Valeurs alignées exactement avec StatutCommande côté backend Java
export const StatutCommande = {
  EN_COURS: "EN_COURS",
  COMPLETEE: "COMPLETEE",
  CONFIRMEE: "CONFIRMEE",
  PAYOUT: "PAYOUT",
  INVOICE_SELLER: "INVOICE_SELLER",
  INVOICE_CUSTOMER: "INVOICE_CUSTOMER",
  FACTURE_MARCHAND_RECUE: "FACTURE_MARCHAND_RECUE",
  FACTURES_CLIENT_ENVOYEES: "FACTURES_CLIENT_ENVOYEES",
  TERMINEE: "TERMINEE",
  ANNULEE: "ANNULEE",
  REMBOURSEE: "REMBOURSEE",
} as const;

export type StatutCommandeType =
  (typeof StatutCommande)[keyof typeof StatutCommande];

/** DTO de commande retourné par les endpoints standard /commandes */
export interface OrderDTO {
  uuid: string;
  numeroCommande?: string;
  utilisateurUuid: string;
  utilisateurNom: string;
  utilisateurPrenom: string;
  dealUuid: string;
  dealTitre: string;
  dealPrixPart: number;
  montantTotal: number;
  statut: StatutCommandeType;
  dateCommande: string;
  dateCreation: string;
  dateModification: string;
  /** Date à laquelle le payout a été déposé (PAYOUT step) */
  dateDepotPayout?: string;
  /** URL MinIO de la facture du marchand */
  factureMarchandUrl?: string;
  paiements?: PaymentDTO[];
}

export interface CreateOrderDTO {
  utilisateurUuid: string;
  dealUuid: string;
  montantTotal: number;
}

export type UpdateOrderDTO = Partial<
  Omit<OrderDTO, "uuid" | "dateCreation" | "dateModification">
>;

/** DTO retourné par les endpoints admin liste /admin/commandes */
export interface CommandeListDTO {
  uuid: string;
  numeroCommande: string;
  marchandUuid: string;
  marchandNom: string;
  marchandPrenom: string;
  marchandEmail: string;
  dealUuid: string;
  dealTitre: string;
  dateCreation: string;
  montantTotalPaiements: number;
  statut: StatutCommandeType;
  /** Date à laquelle le payout a été déposé (PAYOUT step) */
  dateDepotPayout?: string;
  /** URL MinIO de la facture du marchand */
  factureMarchandUrl?: string;
}

export interface CommandeStatsDTO {
  totalCommandes: number;
  commandesConfirmees: number;
  commandesEnCours: number;
  commandesAnnulees: number;
  commandesRemboursees: number;
}

export interface OrderListResponseDTO {
  commandes: CommandeListDTO[];
  statistiques: CommandeStatsDTO;
}

/** DTO pour un utilisateur participant à une commande */
export interface CommandeUtilisateurDTO {
  uuid: string;
  commandeUuid: string;
  utilisateurUuid: string;
  nom: string;
  prenom: string;
  email: string;
  /** Statut brut : EN_ATTENTE ou VALIDEE */
  statutCommandeUtilisateur: string;
  /** true si le statut est VALIDEE */
  valide: boolean;
  /** Montant payé par cet utilisateur pour cette commande */
  montant?: number;
  /** Numéro de transaction du paiement */
  numeroPayment?: string;
}

/** DTO de réponse après validation des factures clients */
export interface ValidationFacturesClientResponseDTO {
  commandeUuid: string;
  numeroCommande: string;
  nombreValidations: number;
  nombreTotal: number;
  toutesValidees: boolean;
  message: string;
}
