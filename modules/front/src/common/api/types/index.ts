// Types pour les entités backend

export enum StatutUtilisateur {
  ACTIF = "ACTIF",
  INACTIF = "INACTIF",
}

export enum RoleUtilisateur {
  UTILISATEUR = "UTILISATEUR",
  VENDEUR = "VENDEUR",
  ADMIN = "ADMIN",
}

export enum StatutDeal {
  BROUILLON = "BROUILLON",
  PUBLIE = "PUBLIE",
  ANNULE = "ANNULE",
  TERMINE = "TERMINE",
}

export interface Utilisateur {
  uuid: string;
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  statut: StatutUtilisateur;
  role: RoleUtilisateur;
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
  statut: StatutDeal;
  createurUuid: string;
  createurNom: string;
  categorieUuid: string;
  categorieNom: string;
  listeImages: string[];
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
  statut: StatutUtilisateur;
  role: RoleUtilisateur;
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