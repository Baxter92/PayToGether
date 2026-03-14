package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.MarchandAvecDealsModele;
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
     * Trouver tous les utilisateurs
     */
    List<UtilisateurModele> lireTousMarchands();

    /**
     * Mettre à jour un utilisateur
     */
    UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur, String token);

    /**
     * Supprimer un utilisateur par son UUID
     */
    void supprimerParUuid(UUID uuid, String token);

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
     * Demander la réinitialisation du mot de passe (étape 1)
     * Génère un token et envoie un événement pour envoyer l'email
     * @param email Email de l'utilisateur
     */
    void demanderReinitialisationMotDePasse(String email);

    /**
     * Activer/Désactiver un utilisateur
     */
    void activerUtilisateur(UUID utilisateurUuid, boolean actif, String token);

    /**
     * Assigner un rôle à un utilisateur
     */
    void assignerRole(UUID utilisateurUuid, String nomRole, String token);

    /**
     * Activer un compte avec validation du token
     * @param token Token de validation reçu par email
     */
    void activerCompteAvecToken(String token);

    /**
     * Réinitialiser le mot de passe avec validation du token
     * @param token Token de réinitialisation reçu par email
     * @param nouveauMotDePasse Nouveau mot de passe
     */
    void reinitialiserMotDePasseAvecToken(String token, String nouveauMotDePasse);

    /**
     * Récupérer tous les marchands avec leurs deals, moyennes et statuts de commandes
     * @return Liste des marchands enrichis
     */
    List<MarchandAvecDealsModele> lireTousMarchandsAvecDeals();
}
