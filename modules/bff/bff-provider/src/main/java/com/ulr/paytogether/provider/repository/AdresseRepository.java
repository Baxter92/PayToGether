package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.AdresseJpa;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Adresse
 */
@Repository
public interface AdresseRepository extends JpaRepository<AdresseJpa, UUID> {

    /**
     * Trouve une adresse par son utilisateur
     * @param paiementJpa
     * @return
     */
    Optional<AdresseJpa> findByPaiement(PaiementJpa paiementJpa);
}
