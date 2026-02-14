import type { ImageResponse } from "../hooks/useImageUpload";

export const StatutDeal = {
  BROUILLON: "BROUILLON",
  PUBLIE: "PUBLIE",
  ANNULE: "ANNULE",
  TERMINE: "TERMINE",
} as const;

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
  statut: typeof StatutDeal;
  createurUuid: string;
  createurNom: string;
  categorieUuid: string;
  categorieNom: string;
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
export type UpdateDealDTO = Partial<
  Omit<
    DealDTO,
    | "uuid"
    | "dateCreation"
    | "dateModification"
    | "createurUuid"
    | "createurNom"
  >
>;
