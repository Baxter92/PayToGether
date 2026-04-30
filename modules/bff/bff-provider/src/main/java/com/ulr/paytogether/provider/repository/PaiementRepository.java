package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Paiement
 * Support Square Payment avec méthodes de recherche additionnelles
 */
@Repository
public interface PaiementRepository extends JpaRepository<PaiementJpa, UUID> {

    /**
     * Recherche tous les paiements triés par date de paiement décroissante
     * Override de findAll() pour ajouter le tri
     */
    @Query("SELECT p FROM PaiementJpa p ORDER BY p.datePaiement DESC")
    List<PaiementJpa> findAll();

    /**
     * Recherche tous les paiements d'un utilisateur triés par date décroissante
     * @param utilisateurJpa l'utilisateur
     * @return la liste des paiements
     */
    List<PaiementJpa> findByUtilisateurJpaOrderByDatePaiementDesc(UtilisateurJpa utilisateurJpa);

    /**
     * Recherche tous les paiements par statut triés par date décroissante
     * @param statut le statut du paiement
     * @return la liste des paiements
     */
    List<PaiementJpa> findByStatutOrderByDatePaiementDesc(StatutPaiement statut);

    /**
     * Recherche tous les paiements d'une commande triés par date décroissante
     * @param commandeJpa la commande
     * @return la liste des paiements
     */
    List<PaiementJpa> findByCommandeJpaOrderByDatePaiementDesc(CommandeJpa commandeJpa);

    /**
     * Recherche tous les paiements d'une commande par UUID triés par date décroissante
     * @param commandeUuid l'UUID de la commande
     * @return la liste des paiements
     */
    List<PaiementJpa> findByCommandeJpaUuidOrderByDatePaiementDesc(UUID commandeUuid);

    /**
     * Recherche un paiement par transactionId
     * @param transactionId l'ID de transaction
     * @return le paiement trouvé
     */
    Optional<PaiementJpa> findByTransactionId(String transactionId);

    /**
     * Recherche un paiement par Square Payment ID
     * @param squarePaymentId l'ID du paiement Square
     * @return le paiement trouvé
     */
    Optional<PaiementJpa> findBySquarePaymentId(String squarePaymentId);

    /**
     * Recherche un paiement par Square Order ID
     * @param squareOrderId l'ID de la commande Square
     * @return le paiement trouvé
     */
    Optional<PaiementJpa> findBySquareOrderId(String squareOrderId);

    /**
     * Recherche le paiement le plus récent d'un utilisateur pour un deal spécifique
     * @param utilisateurUuid UUID de l'utilisateur
     * @param dealUuid UUID du deal
     * @return le paiement trouvé
     */
    Optional<PaiementJpa> findFirstByUtilisateurJpaUuidAndCommandeJpaDealJpaUuidOrderByDatePaiementDesc(UUID utilisateurUuid, UUID dealUuid);

    /**
     * Recherche le paiement d'un utilisateur pour une commande spécifique
     * Utilisé pour récupérer montant et numeroPayment dans la liste des utilisateurs d'une commande
     *
     * @param commandeUuid UUID de la commande
     * @param utilisateurUuid UUID de l'utilisateur
     * @return le paiement trouvé
     */
    Optional<PaiementJpa> findFirstByCommandeJpaUuidAndUtilisateurJpaUuidOrderByDatePaiementDesc(UUID commandeUuid, UUID utilisateurUuid);
}
