import { createResourceService } from "../module/service/resourceFactory";
import type {
  UtilisateurDTO,
  CreateUtilisateurDTO,
  ReinitialiserMotDePasseDTO,
  ActiverUtilisateurDTO,
  AssignerRoleDTO,
} from "../types";
import { apiClient } from "./apiClient";

// Service de base avec méthodes CRUD standard
export const utilisateurBaseService = createResourceService<UtilisateurDTO>(
  apiClient,
  "/utilisateurs",
);

// Service étendu avec méthodes spécifiques
export const utilisateurService = {
  ...utilisateurBaseService,

  // Récupérer un utilisateur par email
  getByEmail: (email: string) =>
    apiClient.get<UtilisateurDTO>(`/utilisateurs/email/${email}`),

  // Vérifier si un email existe
  existsByEmail: (email: string) =>
    apiClient.get<boolean>(`/utilisateurs/existe/${email}`),

  // Confirmer l'upload de la photo de profil
  confirmPhotoUpload: (utilisateurUuid: string) =>
    apiClient.patch<void>(
      `/utilisateurs/${utilisateurUuid}/photo-profil/confirm`,
    ),

  // Obtenir l'URL de lecture de la photo de profil
  getPhotoUrl: (utilisateurUuid: string) =>
    apiClient.get<{ url: string }>(
      `/utilisateurs/${utilisateurUuid}/photo-profil/url`,
    ),

  // Réinitialiser le mot de passe
  resetPassword: (utilisateurUuid: string, dto: ReinitialiserMotDePasseDTO) =>
    apiClient.patch<void>(`/utilisateurs/${utilisateurUuid}/reset-password`, {
      body: dto,
    }),

  // Activer / Désactiver un utilisateur
  setEnabled: (utilisateurUuid: string, dto: ActiverUtilisateurDTO) =>
    apiClient.patch<void>(`/utilisateurs/${utilisateurUuid}/enable`, {
      body: dto,
    }),

  // Assigner un rôle à un utilisateur
  assignRole: (utilisateurUuid: string, dto: AssignerRoleDTO) =>
    apiClient.patch<void>(`/utilisateurs/${utilisateurUuid}/assign-role`, {
      body: dto,
    }),

  // Créer un utilisateur (avec DTO spécifique)
  create: (dto: CreateUtilisateurDTO) =>
    apiClient.post<UtilisateurDTO>("/utilisateurs", { body: dto }),
};
