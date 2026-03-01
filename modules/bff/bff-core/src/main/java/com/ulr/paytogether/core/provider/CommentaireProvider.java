package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.CommentaireModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les commentaires
 */
public interface CommentaireProvider {

    CommentaireModele sauvegarder(CommentaireModele commentaire);

    CommentaireModele mettreAJour(UUID uuid, CommentaireModele commentaire);

    Optional<CommentaireModele> trouverParUuid(UUID uuid);

    List<CommentaireModele> trouverParDeal(UUID dealUuid);

    List<CommentaireModele> trouverParUtilisateur(UUID utilisateurUuid);

    List<CommentaireModele> trouverTous();

    void supprimerParUuid(UUID uuid);

    /**
     * Trouver toutes les réponses d'un commentaire parent
     */
    List<CommentaireModele> trouverReponsesParCommentaireParent(UUID commentaireParentUuid);

    /**
     * Mettre à jour le flag pertinent d'une réponse
     */
    void mettreAJourFlagPertinent(UUID uuid, Boolean estPertinent);
}
