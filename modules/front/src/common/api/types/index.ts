// Types pour les entités backend

import type { ImageResponse } from "../hooks/useImageUpload";

export const StatutUtilisateur = {
  ACTIF: "ACTIF",
  INACTIF: "INACTIF",
} as const;

export const RoleUtilisateur = {
  UTILISATEUR: "UTILISATEUR",
  VENDEUR: "VENDEUR",
  ADMIN: "ADMIN",
} as const;

export const StatutDeal = {
  BROUILLON: "BROUILLON",
  PUBLIE: "PUBLIE",
  ANNULE: "ANNULE",
  TERMINE: "TERMINE",
} as const;

export interface Utilisateur {
  uuid: string;
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  statut: typeof StatutUtilisateur;
  role: typeof RoleUtilisateur;
  photoProfil: string;
  dateCreation: string;
  dateModification: string;
}

export interface Categorie {
  uuid: string;
  nom: string;
  description: string;
  icone: string;
  dateCreation: string;
  dateModification: string;
}

export interface Deal {
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

export interface CreerUtilisateurDTO {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  statut: typeof StatutUtilisateur;
  role: typeof RoleUtilisateur;
  photoProfil: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  meta: {
    page: number;
    limit: number;
    total?: number;
    nextCursor?: string | null;
  };
}
