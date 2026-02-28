package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * @param utilisateurJpa le créateur
     * @return la liste des deals
     */
    List<DealJpa> findByMarchandJpa(UtilisateurJpa utilisateurJpa);

    /**
     * Recherche tous les deals d'une catégorie
     * @param categorieJpa la catégorie
     * @return la liste des deals
     */
    List<DealJpa> findByCategorieJpa(CategorieJpa categorieJpa);

    /**
     * Supprime tous les points forts d'un deal
     * @param deal_uuid l'UUID du deal
     */
    @Modifying
    @Query(value = "DELETE FROM public.deal WHERE uuid = :deal_uuid", nativeQuery = true)
    void deleteDeal(@Param("deal_uuid") UUID deal_uuid);
}
