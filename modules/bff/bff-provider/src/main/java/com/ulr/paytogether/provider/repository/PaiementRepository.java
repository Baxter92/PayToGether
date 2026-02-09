package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
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
     * @param utilisateurJpa l'utilisateur
     * @return la liste des paiements
     */
    List<PaiementJpa> findByUtilisateurJpa(UtilisateurJpa utilisateurJpa);

    /**
     * Recherche tous les paiements par statut
     * @param statut le statut du paiement
     * @return la liste des paiements
     */
    List<PaiementJpa> findByStatut(StatutPaiement statut);

    /**
     * Recherche tous les paiements d'une commande
     * @param commandeJpa la commande
     * @return la liste des paiements
     */
    List<PaiementJpa> findByCommandeJpa(CommandeJpa commandeJpa);
}
