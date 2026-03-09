export const StatutCommande = {
  EN_ATTENTE: "EN_ATTENTE",
  EN_COURS: "EN_COURS",
  LIVRÉE: "LIVRÉE",
  ANNULÉE: "ANNULÉE",
  REMBOURSÉE: "REMBOURSÉE",
} as const;

export type StatutCommandeType =
  (typeof StatutCommande)[keyof typeof StatutCommande];

export interface OrderDTO {
  uuid: string;
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
  paiements?: PaymentDTO[];
}

export interface CreateOrderDTO {
  utilisateurUuid: string;
  dealUuid: string;
  montantTotal: number;
  statut: typeof StatutCommande.EN_ATTENTE;
}

export type UpdateOrderDTO = Partial<
  Omit<OrderDTO, "uuid" | "dateCreation" | "dateModification">
>;

// Type pour la réponse des listes de commandes (admin)
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

// Import du type Payment si nécessaire
export interface PaymentDTO {
  uuid: string;
  clientUuid: string;
  clientNom: string;
  clientPrenom: string;
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
  methodePaiement: string;
  statutPaiement: string;
}
