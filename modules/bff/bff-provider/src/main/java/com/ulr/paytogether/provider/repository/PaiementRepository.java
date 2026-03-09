package com.ulr.paytogether.provider.repository;

import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import org.springframework.data.jpa.repository.JpaRepository;
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

    /**
     * Recherche tous les paiements d'une commande par UUID
     * @param commandeUuid l'UUID de la commande
     * @return la liste des paiements
     */
    List<PaiementJpa> findByCommandeJpaUuid(UUID commandeUuid);

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
}
