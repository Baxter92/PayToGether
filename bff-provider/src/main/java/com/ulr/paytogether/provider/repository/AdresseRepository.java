package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.AdresseJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Adresse
 */
@Repository
public interface AdresseRepository extends JpaRepository<AdresseJpa, UUID> {

    /**
     * Recherche toutes les adresses d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @return la liste des adresses
     */
    List<AdresseJpa> findByUtilisateurUuid(UUID utilisateurUuid);

    /**
     * Recherche l'adresse principale d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @param principale true pour l'adresse principale
     * @return un Optional contenant l'adresse principale si elle existe
     */
    Optional<AdresseJpa> findByUtilisateurUuidAndPrincipale(UUID utilisateurUuid, Boolean principale);
}
