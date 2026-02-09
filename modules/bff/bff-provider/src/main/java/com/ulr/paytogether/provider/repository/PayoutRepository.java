package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.PayoutJpa;
import com.ulr.paytogether.core.enumeration.StatutPayout;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Payout
 */
@Repository
public interface PayoutRepository extends JpaRepository<PayoutJpa, UUID> {

    /**
     * Recherche tous les payouts d'un vendeur
     * @param utilisateurJpa le vendeur
     * @return la liste des payouts
     */
    List<PayoutJpa> findByMarchand(UtilisateurJpa utilisateurJpa);

    /**
     * Recherche tous les payouts par statut
     * @param statut le statut du payout
     * @return la liste des payouts
     */
    List<PayoutJpa> findByStatut(StatutPayout statut);
}
