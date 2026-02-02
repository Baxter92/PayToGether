package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Commentaire
 */
@Repository
public interface CommentaireRepository extends JpaRepository<CommentaireJpa, UUID> {

    /**
     * Recherche tous les commentaires d'un deal
     * @param dealUuid l'UUID du deal
     * @return la liste des commentaires
     */
    List<CommentaireJpa> findByDealUuid(UUID dealUuid);

    /**
     * Recherche tous les commentaires d'un auteur
     * @param auteurUuid l'UUID de l'auteur
     * @return la liste des commentaires
     */
    List<CommentaireJpa> findByAuteurUuid(UUID auteurUuid);

    /**
     * Recherche toutes les réponses d'un commentaire parent
     * @param commentaireParentUuid l'UUID du commentaire parent
     * @return la liste des réponses
     */
    List<CommentaireJpa> findByCommentaireParentUuid(UUID commentaireParentUuid);
}
