package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.ImagePub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité ImagePub
 */
@Repository
public interface ImagePubRepository extends JpaRepository<ImagePub, UUID> {

    /**
     * Recherche toutes les images d'une publicité
     * @param publiciteUuid l'UUID de la publicité
     * @return la liste des images
     */
    List<ImagePub> findByPubliciteUuid(UUID publiciteUuid);

    /**
     * Recherche l'image principale d'une publicité
     * @param publiciteUuid l'UUID de la publicité
     * @param principale true pour l'image principale
     * @return un Optional contenant l'image principale si elle existe
     */
    Optional<ImagePub> findByPubliciteUuidAndPrincipale(UUID publiciteUuid, Boolean principale);
}
