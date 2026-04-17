package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.modele.PageModele;

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

    /**
     * Trouver les commentaires d'un deal avec pagination
     * @param dealUuid UUID du deal
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de commentaires
     */
    PageModele<CommentaireModele> trouverParDeal(UUID dealUuid, int page, int size);

    List<CommentaireModele> trouverParUtilisateur(UUID utilisateurUuid);

    List<CommentaireModele> trouverTous();

    /**
     * Trouver tous les commentaires avec pagination
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de commentaires
     */
    PageModele<CommentaireModele> trouverTous(int page, int size);

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
