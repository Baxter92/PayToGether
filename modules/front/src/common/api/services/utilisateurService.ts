import { apiClient } from "../index";
import { createResourceService } from "../service/resourceFactory";
import type { Utilisateur, CreerUtilisateurDTO } from "../types";

// Service de base avec méthodes CRUD standard
export const utilisateurBaseService = createResourceService<Utilisateur>(
  apiClient,
  "/utilisateurs",
);

// Service étendu avec méthodes spécifiques
export const utilisateurService = {
  ...utilisateurBaseService,

  // Récupérer un utilisateur par email
  getByEmail: (email: string) =>
    apiClient.get<Utilisateur>(`/utilisateurs/email/${email}`),

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

  // Créer un utilisateur (avec DTO spécifique)
  create: (dto: CreerUtilisateurDTO) =>
    apiClient.post<Utilisateur>("/utilisateurs", { body: dto }),
};
