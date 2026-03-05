package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.ValidationTokenModele;

import java.util.Optional;
import java.util.UUID;

/**
 * Provider pour les tokens de validation (Port de sortie)
 */
public interface ValidationTokenProvider {

    /**
     * Trouver un token par sa valeur
     */
    Optional<ValidationTokenModele> trouverParToken(String token);

    /**
     * Trouver un token par l'UUID de l'utilisateur
     */
    Optional<ValidationTokenModele> trouverParUtilisateur(UUID utilisateurUuid);

    /**
     * Marquer un token comme utilisé
     */
    void marquerCommeUtilise(String token);

    /**
     * Sauvegarder un token
     */
    ValidationTokenModele sauvegarder(ValidationTokenModele token);

    /**
     * Supprimer les tokens expirés
     * @return Nombre de tokens supprimés
     */
    int supprimerTokensExpires();
}

