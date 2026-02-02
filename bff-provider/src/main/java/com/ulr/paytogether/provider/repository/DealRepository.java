package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.Deal;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Deal
 */
@Repository
public interface DealRepository extends JpaRepository<Deal, UUID> {

    /**
     * Recherche tous les deals par statut
     * @param statut le statut du deal
     * @return la liste des deals
     */
    List<Deal> findByStatut(StatutDeal statut);

    /**
     * Recherche tous les deals d'un créateur
     * @param createurUuid l'UUID du créateur
     * @return la liste des deals
     */
    List<Deal> findByCreateurUuid(UUID createurUuid);

    /**
     * Recherche tous les deals d'une catégorie
     * @param categorieUuid l'UUID de la catégorie
     * @return la liste des deals
     */
    List<Deal> findByCategorieUuid(UUID categorieUuid);
}
