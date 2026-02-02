package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.Publicite;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutPublicite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Publicite
 */
@Repository
public interface PubliciteRepository extends JpaRepository<Publicite, UUID> {

    /**
     * Recherche toutes les publicités par statut
     * @param statut le statut de la publicité
     * @return la liste des publicités
     */
    List<Publicite> findByStatut(StatutPublicite statut);

    /**
     * Recherche toutes les publicités d'un annonceur
     * @param annonceurUuid l'UUID de l'annonceur
     * @return la liste des publicités
     */
    List<Publicite> findByAnnonceurUuid(UUID annonceurUuid);
}
