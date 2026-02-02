package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.ImageDealJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité ImageDeal
 */
@Repository
public interface ImageDealRepository extends JpaRepository<ImageDealJpa, UUID> {

    /**
     * Recherche toutes les images d'un deal
     * @param dealUuid l'UUID du deal
     * @return la liste des images
     */
    List<ImageDealJpa> findByDealUuid(UUID dealUuid);

    /**
     * Recherche l'image principale d'un deal
     * @param dealUuid l'UUID du deal
     * @param principale true pour l'image principale
     * @return un Optional contenant l'image principale si elle existe
     */
    Optional<ImageDealJpa> findByDealUuidAndPrincipale(UUID dealUuid, Boolean principale);
}
