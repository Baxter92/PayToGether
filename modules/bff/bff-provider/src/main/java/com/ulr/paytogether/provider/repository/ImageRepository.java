package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.ImageJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageJpa, UUID> {
    /**
     * Recherche une image par son URL
     * @param urlImage l'URL de l'image
     * @return l'image correspondante
     */
    ImageJpa findByUrlImage(String urlImage);
}
