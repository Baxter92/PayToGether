package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité Paiement
 */
@Repository
public interface PaiementRepository extends JpaRepository<PaiementJpa, UUID> {

    /**
     * Recherche tous les paiements d'un utilisateur
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @return la liste des paiements
     */
    List<PaiementJpa> findByUtilisateurUuid(UUID utilisateurUuid);

    /**
     * Recherche tous les paiements d'un deal
     * @param dealUuid l'UUID du deal
     * @return la liste des paiements
     */
    List<PaiementJpa> findByDealUuid(UUID dealUuid);

    /**
     * Recherche tous les paiements par statut
     * @param statut le statut du paiement
     * @return la liste des paiements
     */
    List<PaiementJpa> findByStatut(StatutPaiement statut);

    /**
     * Recherche tous les paiements d'une commande
     * @param commandeUuid l'UUID de la commande
     * @return la liste des paiements
     */
    List<PaiementJpa> findByCommandeUuid(UUID commandeUuid);
}
