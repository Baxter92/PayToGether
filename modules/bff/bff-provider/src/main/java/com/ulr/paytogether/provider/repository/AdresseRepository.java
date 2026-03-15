package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.AdresseJpa;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
     * Trouve une adresse par son utilisateur
     * @param paiementJpa
     * @return
     */
    Optional<AdresseJpa> findByPaiement(PaiementJpa paiementJpa);

    /**
     * Trouve une adresse par l'UUID du paiement
     * @param paiementUuid UUID du paiement
     * @return l'adresse trouvée
     */
    Optional<AdresseJpa> findByPaiementUuid(UUID paiementUuid);

    @Query(value = "DELETE FROM AdresseJpa a WHERE a.paiement IN :paiements")
    void deleteAllByPaiement(List<PaiementJpa> paiements);
}
