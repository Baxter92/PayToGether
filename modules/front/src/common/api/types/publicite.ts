export type StatutImageType = "PENDING" | "UPLOADED" | "FAILED";

export interface PubliciteImageDTO {
  imageUuid?: string;
  urlImage: string;
  presignUrl?: string | null;
  statut?: StatutImageType | null;
  file?: File;
}

export interface PubliciteDTO {
  uuid: string;
  titre: string;
  description: string;
  lienExterne?: string | null;
  listeImages: PubliciteImageDTO[];
  dateDebut: string;
  dateFin: string;
  active?: boolean | null;
  dateCreation: string;
  dateModification: string;
}

export interface CreatePubliciteDTO {
  titre: string;
  description: string;
  lienExterne?: string | null;
  listeImages: PubliciteImageDTO[];
  dateDebut: string;
  dateFin: string;
  active?: boolean | null;
}

export type UpdatePubliciteDTO = Partial<
  Omit<PubliciteDTO, "uuid" | "dateCreation" | "dateModification">
>;
