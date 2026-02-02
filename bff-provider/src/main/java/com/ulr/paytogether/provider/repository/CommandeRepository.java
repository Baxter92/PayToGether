package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.core.domaine.entite.Commande;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Commande
 */
@Repository
public interface CommandeRepository extends JpaRepository<Commande, UUID> {

    /**
     * Recherche une commande par numéro
     * @param numeroCommande le numéro de commande
     * @return un Optional contenant la commande si elle existe
     */
    Optional<Commande> findByNumeroCommande(String numeroCommande);

    /**
     * Recherche toutes les commandes d'un utilisateur (vendeur)
     * @param utilisateurUuid l'UUID de l'utilisateur
     * @return la liste des commandes
     */
    List<Commande> findByUtilisateurUuid(UUID utilisateurUuid);

    /**
     * Recherche toutes les commandes d'un deal
     * @param dealUuid l'UUID du deal
     * @return la liste des commandes
     */
    List<Commande> findByDealUuid(UUID dealUuid);

    /**
     * Recherche toutes les commandes par statut
     * @param statut le statut de la commande
     * @return la liste des commandes
     */
    List<Commande> findByStatut(StatutCommande statut);
}
