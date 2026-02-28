package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.ImageDealJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository pour l'entité ImageDeal
 */
@Repository
public interface ImageDealRepository extends JpaRepository<ImageDealJpa, UUID> {

    /**
     * Recherche une image par son URL
     * @param urlImage l'URL de l'image
     * @return un Optional contenant l'image si elle existe
     */
    ImageDealJpa findByUrlImage(String urlImage);
}
