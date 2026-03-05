package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.ValidationTokenModele;

import java.util.Optional;
import java.util.UUID;

/**
 * Service métier pour la gestion des tokens de validation.
 * Utilisé pour la validation de compte, réinitialisation de mot de passe, etc.
 */
public interface ValidationTokenService {

    /**
     * Crée un nouveau token de validation
     *
     * @param tokenModele Le modèle du token à créer
     * @return Le token créé
     */
    ValidationTokenModele creer(ValidationTokenModele tokenModele);

    /**
     * Trouve un token par sa valeur
     *
     * @param token La valeur du token
     * @return Le token s'il existe
     */
    Optional<ValidationTokenModele> trouverParToken(String token);

    /**
     * Trouve un token par l'UUID de l'utilisateur
     *
     * @param utilisateurUuid UUID de l'utilisateur
     * @return Le token s'il existe
     */
    Optional<ValidationTokenModele> trouverParUtilisateur(UUID utilisateurUuid);

    /**
     * Marque un token comme utilisé
     *
     * @param token La valeur du token
     */
    void marquerCommeUtilise(String token);

    /**
     * Supprime les tokens expirés
     *
     * @return Nombre de tokens supprimés
     */
    int supprimerTokensExpires();
}

