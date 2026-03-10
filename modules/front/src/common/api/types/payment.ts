export const StatutPaiement = {
  EN_ATTENTE: "EN_ATTENTE",
  CONFIRME: "CONFIRME",
  ECHOUE: "ECHOUE",
  PROCESSING: "PROCESSING",
  REFUNDED: "REFUNDED",
  CANCELLED: "CANCELLED",
} as const;

export const MethodePaiement = {
  CARTE_CREDIT: "CARTE_CREDIT",
  INTERAC: "INTERAC",
  VIREMENT_BANCAIRE: "VIREMENT_BANCAIRE",
  SQUARE_CARD: "SQUARE_CARD",
  SQUARE_GOOGLE_PAY: "SQUARE_GOOGLE_PAY",
  SQUARE_APPLE_PAY: "SQUARE_APPLE_PAY",
  SQUARE_CASH_APP_PAY: "SQUARE_CASH_APP_PAY",
} as const;

export type StatutPaiementType =
  (typeof StatutPaiement)[keyof typeof StatutPaiement];
export type MethodePaiementType =
  (typeof MethodePaiement)[keyof typeof MethodePaiement];

export interface PaymentDTO {
  uuid: string;
  clientUuid: string;
  clientNom: string;
  clientPrenom: string;
  clientEmail: string;
  commandeUuid: string;
  numeroCommande: string;
  dealUuid: string;
  dealTitre: string;
  marchandUuid: string;
  marchandNom: string;
  marchandPrenom: string;
  marchandEmail: string;
  montant: number;
  datePaiement: string;
  nombreDePart: number;
  methodePaiement: MethodePaiementType;
  statutPaiement: StatutPaiementType;
  dateCreation?: string;
  dateModification?: string;
}

// DTO pour créer un paiement Square
export interface CreateSquarePaymentDTO {
  dealUuid: string;
  utilisateurUuid: string;
  montant: number;
  squareToken: string;
  methodePaiement: string; // SQUARE_CARD, SQUARE_GOOGLE_PAY, etc.
  locationId?: string;
  nombreDePart?: number;
  adresse?: {
    rue: string;
    codePostal: string;
    ville: string;
    province: string;
    pays: string;
  };
}

// Réponse de création de paiement Square
export interface SquarePaymentResponseDTO {
  uuid: string;
  montant: number;
  statut: StatutPaiementType;
  methodePaiement: MethodePaiementType;
  transactionId: string;
  squarePaymentId?: string;
  squareOrderId?: string;
  squareLocationId?: string;
  squareReceiptUrl?: string;
  messageErreur?: string;
  utilisateurUuid: string;
  commandeUuid: string;
  datePaiement: string;
  dateCreation: string;
  dateModification: string;
}

// Type pour les statistiques de paiements (admin)
export interface PaiementStatsDTO {
  totalTransactions: number;
  transactionsReussies: number;
  transactionsEchouees: number;
  montantTotal: number;
}

export interface PaiementListDTO {
  uuid: string;
  clientUuid: string;
  clientNom: string;
  clientPrenom: string;
  clientEmail: string;
  commandeUuid: string;
  numeroCommande: string;
  dealUuid: string;
  dealTitre: string;
  marchandUuid: string;
  marchandNom: string;
  marchandPrenom: string;
  marchandEmail: string;
  montant: number;
  datePaiement: string;
  nombreDePart: number;
  methodePaiement: MethodePaiementType;
  statutPaiement: StatutPaiementType;
}

export interface PaiementListResponseDTO {
  paiements: PaiementListDTO[];
  statistiques: PaiementStatsDTO;
}
