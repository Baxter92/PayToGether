package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.AdresseJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
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
     * @param utilisateurJpa l'utilisateur
     * @return la liste des adresses
     */
    List<AdresseJpa> findByUtilisateurJpa(UtilisateurJpa utilisateurJpa);
}
