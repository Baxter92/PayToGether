package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.ImageUtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageUtilisateurRepository extends JpaRepository<ImageUtilisateurJpa, UUID> {
    /**
     * Recherche une image utilisateur par son URL
     * @param urlImage l'URL de l'image
     * @return l'image utilisateur correspondante
     */
    ImageUtilisateurJpa findByUrlImage(String urlImage);
}
