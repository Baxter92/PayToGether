package com.ulr.paytogether.provider.repository;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import com.ulr.paytogether.core.enumeration.StatutPublicite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
/**
 * Repository pour l'entité Publicite
 */
@Repository
public interface PubliciteRepository extends JpaRepository<PubliciteJpa, UUID> {
    /**
     * Recherche toutes les publicités par statut
     * @param statut le statut de la publicité
     * @return la liste des publicités
     */
    List<PubliciteJpa> findByStatut(StatutPublicite statut);
    /**
     * Trouver les publicités actives
     */
    List<PubliciteJpa> findByActiveTrue();
    /**
     * Recherche toutes les publicités d'un annonceur
     * @param annonceurUuid l'UUID de l'annonceur
     * @return la liste des publicités
     */
    List<PubliciteJpa> findByAnnonceurUuid(UUID annonceurUuid);
}
