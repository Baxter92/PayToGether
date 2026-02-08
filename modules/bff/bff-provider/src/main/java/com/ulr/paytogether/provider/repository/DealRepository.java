package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Deal
 */
@Repository
public interface DealRepository extends JpaRepository<DealJpa, UUID> {

    /**
     * Recherche tous les deals par statut
     * @param statut le statut du deal
     * @return la liste des deals
     */
    List<DealJpa> findByStatut(StatutDeal statut);

    /**
     * Recherche tous les deals d'un créateur
     * @param createurUuid l'UUID du créateur
     * @return la liste des deals
     */
    List<DealJpa> findByCreateurUuid(UUID createurUuid);

    /**
     * Recherche tous les deals d'une catégorie
     * @param categorieUuid l'UUID de la catégorie
     * @return la liste des deals
     */
    List<DealJpa> findByCategorieUuid(UUID categorieUuid);
}
