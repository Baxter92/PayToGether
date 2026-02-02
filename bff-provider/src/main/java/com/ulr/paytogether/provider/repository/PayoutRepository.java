package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.Payout;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Payout
 */
@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    /**
     * Recherche un payout par numéro
     * @param numeroPayout le numéro de payout
     * @return un Optional contenant le payout s'il existe
     */
    Optional<Payout> findByNumeroPayout(String numeroPayout);

    /**
     * Recherche tous les payouts d'un vendeur
     * @param vendeurUuid l'UUID du vendeur
     * @return la liste des payouts
     */
    List<Payout> findByVendeurUuid(UUID vendeurUuid);

    /**
     * Recherche tous les payouts par statut
     * @param statut le statut du payout
     * @return la liste des payouts
     */
    List<Payout> findByStatut(StatutPayout statut);
}
