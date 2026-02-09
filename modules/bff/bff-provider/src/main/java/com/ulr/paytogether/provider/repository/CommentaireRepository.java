package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
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
     * @param dealJpa
     * @return
     */
    List<CommentaireJpa> findByDealJpa(DealJpa dealJpa);

    /**
     * Recherche tous les commentaires d'un auteur
     * @param utilisateurJpa
     * @return
     */
    List<CommentaireJpa> findByUtilisateurJpa(UtilisateurJpa utilisateurJpa);

}
