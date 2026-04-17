package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * @param dealJpa
     * @return
     */
    List<CommentaireJpa> findByDealJpa(DealJpa dealJpa);

    /**
     * Recherche tous les commentaires d'un deal avec pagination
     * @param dealJpa le deal
     * @param pageable paramètres de pagination
     * @return page de commentaires
     */
    Page<CommentaireJpa> findByDealJpa(DealJpa dealJpa, Pageable pageable);

    /**
     * Recherche tous les commentaires d'un auteur
     * @param utilisateurJpa
     * @return
     */
    List<CommentaireJpa> findByUtilisateurJpa(UtilisateurJpa utilisateurJpa);

    /**
     * Recherche toutes les réponses d'un commentaire parent
     * @param commentaireParentJpa le commentaire parent
     * @return liste des réponses (commentaires enfants)
     */
    List<CommentaireJpa> findByCommentaireParentJpa(CommentaireJpa commentaireParentJpa);

    /**
     * Calcule la moyenne des notes pour un deal
     * @param dealUuid UUID du deal
     * @return Moyenne des notes (null si aucun commentaire)
     */
    @Query("SELECT AVG(c.note) FROM CommentaireJpa c WHERE c.dealJpa.uuid = :dealUuid AND c.note IS NOT NULL")
    Double calculerMoyenneNotesPourDeal(@Param("dealUuid") UUID dealUuid);

}
