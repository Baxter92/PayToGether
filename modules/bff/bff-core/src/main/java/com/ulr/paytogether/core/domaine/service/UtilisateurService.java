package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.UtilisateurModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UtilisateurService {
    /**
     * Sauvegarder un utilisateur
     */
    UtilisateurModele creer(UtilisateurModele utilisateur);

    /**
     * Trouver un utilisateur par son UUID
     */
    Optional<UtilisateurModele> lireParUuid(UUID uuid);

    /**
     * Trouver un utilisateur par son email
     */
    Optional<UtilisateurModele> lireParEmail(String email);

    /**
     * Trouver tous les utilisateurs
     */
    List<UtilisateurModele> lireTous();

    /**
     * Mettre à jour un utilisateur
     */
    UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur, String token);

    /**
     * Supprimer un utilisateur par son UUID
     */
    void supprimerParUuid(UUID uuid);

    /**
     * Vérifier si un email existe
     */
    boolean existeParEmail(String email);

    /**
     * Mettre à jour le statut de la photo de profil d'un utilisateur
     */
    void mettreAJourStatutPhotoProfil(UUID utilisateurUuid, StatutImage statut);

    /**
     * Obtenir l'URL de lecture de la photo de profil d'un utilisateur
     */
    String obtenirUrlLecturePhotoProfil(UUID utilisateurUuid);

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    void reinitialiserMotDePasse(UUID utilisateurUuid, String nouveauMotDePasse, String token);

    /**
     * Activer/Désactiver un utilisateur
     */
    void activerUtilisateur(UUID utilisateurUuid, boolean actif, String token);

    /**
     * Assigner un rôle à un utilisateur
     */
    void assignerRole(UUID utilisateurUuid, String nomRole, String token);
}
