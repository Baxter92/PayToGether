package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Commande
 */
@Repository
public interface CommandeRepository extends JpaRepository<CommandeJpa, UUID> {

    /**
     * Recherche toutes les commandes triées par date de création décroissante
     * Override de findAll() pour ajouter le tri
     */
    @Query("SELECT c FROM CommandeJpa c ORDER BY c.dateCreation DESC")
    List<CommandeJpa> findAll();

    /**
     * Recherche une commande par numéro
     * @param numeroCommande le numéro de commande
     * @return un Optional contenant la commande si elle existe
     */
    Optional<CommandeJpa> findByNumeroCommande(String numeroCommande);

    /**
     * Recherche toutes les commandes d'un utilisateur (vendeur) triées par date décroissante
     * @param marchandJpa l'utilisateur marchand
     * @return la liste des commandes
     */
    List<CommandeJpa> findByMarchandJpaOrderByDateCreationDesc(UtilisateurJpa marchandJpa);

    /**
     * Recherche toutes les commandes d'un deal
     * @param dealJpa le deal
     * @return la liste des commandes
     */
    Optional<CommandeJpa> findByDealJpa(DealJpa dealJpa);

    /**
     * Recherche toutes les commandes par statut
     * @param statut le statut de la commande
     * @return la liste des commandes
     */
    List<CommandeJpa> findByStatut(StatutCommande statut);

    /**
     * Recherche une commande par l'UUID d'un paiement associé
     * Utilise une requête JPQL en partant de PaiementJpa
     * @param paiementUuid l'UUID du paiement
     * @return un Optional contenant la commande si elle existe
     */
    @Query("SELECT p.commandeJpa FROM PaiementJpa p WHERE p.uuid = :paiementUuid")
    Optional<CommandeJpa> findByPaiementUuid(@Param("paiementUuid") UUID paiementUuid);
}
