import type { ImageResponse } from "../hooks/useImageUpload";
import type { StatutCommandeType } from "./order";
import type { MethodePaiementType, StatutPaiementType } from "./payment";

export const StatutDeal = {
  BROUILLON: "BROUILLON",
  PUBLIE: "PUBLIE",
  ANNULE: "ANNULE",
  TERMINE: "TERMINE",
} as const;

export type StatutDealType = (typeof StatutDeal)[keyof typeof StatutDeal];

export interface DealDTO {
  uuid: string;
  titre: string;
  description: string;
  prixDeal: number;
  prixPart: number;
  nbParticipants: number;
  dateDebut: string;
  dateFin: string;
  dateExpiration: string;
  statut: StatutDealType;
  createurUuid: string;
  createurNom: string;
  categorieUuid: string;
  categorieNom: string;
  nombrePartsAchetees: number;
  nombreParticipantsReel: number;
  moyenneCommentaires: number;
  listeImages: Partial<ImageResponse>[];
  listePointsForts: string[];
  ville: string;
  pays: string;
  dateCreation: string;
  dateModification: string;
}

export interface CreateDealDTO {
  titre: string;
  description: string;
  prixDeal: number;
  prixPart: number;
  nbParticipants: number;
  dateDebut: string;
  dateFin: string;
  dateExpiration: string;
  statut: typeof StatutDeal.BROUILLON;
  createurUuid: string;
  categorieUuid: string;
  listeImages: Partial<ImageResponse>[];
  listePointsForts: string[];
  ville: string;
  pays: string;
}
export interface UpdateDealDTO {
  titre?: string;
  description?: string;
  prixDeal?: number;
  prixPart?: number;
  nbParticipants?: number;
  dateDebut?: string;
  dateFin?: string;
  dateExpiration?: string;
  statut?: StatutDealType;
  createurUuid?: string;
  categorieUuid?: string;
  listeImages?: Partial<ImageResponse>[];
  listePointsForts?: string[];
  ville?: string;
  pays?: string;
}

export interface ParticipantDto {
  dealUuid: string;
  utilisateurUuid: string;
  utilisateurNom: string;
  utilisateurPrenom: string;
  utilisateurEmail: string;
  nombreDePart: number;
  dateParticipation: string;
  montantTotal: number;
  statutPaiement: StatutPaiementType;
  adresseRue: string;
  adresseCodePostal: string;
  adresseVille: string;
  adressePays: string;
  adresseProvince: string;
  adresseAppartement: string;
  adresseNumeroPhone: string;
}

export interface MyPaymentsDTO {
  paiementUuid: string;
  montantPaiement: number;
  statutPaiement: StatutPaiementType;
  methodePaiement: MethodePaiementType;
  transactionId: string;
  commandeUuid: string;
  numeroCommande: string;
  statutCommande: StatutCommandeType;
  montantTotal: number;
  nbPartsAchetees: number;
  deal: {
    uuid: string;
    titre: string;
    description: string;
    imagePrincipaleUrlPresignee: string;
    dateExpiration: string;
    prixDeal: number;
    prixPart: number;
  };
  categorie: {
    uuid: string;
    nom: string;
    icone: string;
  };
  adresseFacturation: {
    rue: string;
    codePostal: string;
    ville: string;
    pays: string;
    uuid: string;
    telephone: string;
  };
}
