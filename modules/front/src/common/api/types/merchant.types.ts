/**
 * Types pour les marchands enrichis avec leurs deals
 */

export const StatutUtilisateur = {
  ACTIF: "ACTIF",
  INACTIF: "INACTIF",
  SUSPENDU: "SUSPENDU",
} as const;

export type StatutUtilisateur = typeof StatutUtilisateur[keyof typeof StatutUtilisateur];

export const StatutDeal = {
  BROUILLON: "BROUILLON",
  PUBLIE: "PUBLIE",
  EXPIRE: "EXPIRE",
} as const;

export type StatutDeal = typeof StatutDeal[keyof typeof StatutDeal];

export const StatutCommande = {
  EN_COURS: "EN_COURS",
  COMPLETEE: "COMPLETEE",
  PAYOUT: "PAYOUT",
  INVOICE_SELLER: "INVOICE_SELLER",
  INVOICE_CUSTOMER: "INVOICE_CUSTOMER",
  CONFIRMEE: "CONFIRMEE",
  ANNULEE: "ANNULEE",
  REMBOURSEE: "REMBOURSEE",
  FACTURE_MARCHAND_RECUE: "FACTURE_MARCHAND_RECUE",
  FACTURES_CLIENT_ENVOYEES: "FACTURES_CLIENT_ENVOYEES",
  TERMINEE: "TERMINEE",
} as const;

export type StatutCommande = typeof StatutCommande[keyof typeof StatutCommande];

/**
 * Deal avec son statut de commande et ses statistiques
 */
export interface DealAvecStatut {
  uuid: string;
  titre: string;
  description: string;
  prixDeal: number;
  prixPart: number;
  nbParticipants: number;
  dateDebut: string;
  dateFin: string;
  statut: StatutDeal;
  ville: string;
  pays: string;
  dateCreation: string;
  moyenneCommentaires: number;
  nombreParticipantsReel: number;
  nombrePartsAchetees: number;
  statutCommande: StatutCommande | null;
  imageUrl: string | null;
}

/**
 * Marchand avec ses deals enrichis
 */
export interface MarchandAvecDeals {
  uuid: string;
  nom: string;
  prenom: string;
  email: string;
  statut: StatutUtilisateur;
  role: string;
  photoProfil: string | null;
  dateCreation: string;
  dateModification: string;
  moyenneGlobale: number;
  nombreDeals: number;
  deals: DealAvecStatut[];
}

/**
 * Statistiques des marchands
 */
export interface StatistiquesMarchands {
  totalMarchands: number;
  marchandsActifs: number;
  marchandsInactifs: number;
}

/**
 * Réponse de l'API avec marchands et statistiques
 */
export interface MarchandsAvecStatistiques {
  totalMarchands: number;
  marchandsActifs: number;
  marchandsInactifs: number;
  marchands: MarchandAvecDeals[];
}

