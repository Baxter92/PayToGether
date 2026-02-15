export const StatutUtilisateur = {
  ACTIF: "ACTIF",
  INACTIF: "INACTIF",
} as const;

export const RoleUtilisateur = {
  UTILISATEUR: "UTILISATEUR",
  VENDEUR: "VENDEUR",
  ADMIN: "ADMIN",
} as const;

export type StatutUtilisateurType =
  (typeof StatutUtilisateur)[keyof typeof StatutUtilisateur];
export type RoleUtilisateurType =
  (typeof RoleUtilisateur)[keyof typeof RoleUtilisateur];

export interface UtilisateurDTO {
  uuid: string;
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  statut: StatutUtilisateurType;
  role: RoleUtilisateurType;
  photoProfil?: string | null;
  dateCreation?: string;
  dateModification?: string;
}

// Backend: CreerUtilisateurDTO
export interface CreateUtilisateurDTO {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  photoProfil?: string;
  statut: StatutUtilisateurType;
  role: RoleUtilisateurType;
}

// Backend: UtilisateurDTO utilise en update (PUT /utilisateurs/{uuid})
export type UpdateUtilisateurDTO = Omit<UtilisateurDTO, "uuid">;
