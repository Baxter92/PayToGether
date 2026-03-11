package com.ulr.paytogether.core.provider;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port (interface) pour les opérations avec Square Payment API
 * Cette interface sera implémentée dans bff-wsclient
 */
public interface SquarePaymentProvider {

    /**
     * Crée un paiement dans Square
     * @param squareToken le token de paiement généré par le frontend
     * @param montant le montant du paiement
     * @param locationId l'ID du lieu Square
     * @param referenceId l'ID de référence (UUID de la commande)
     * @return l'ID du paiement Square
     */
    String creerPaiement(String squareToken, BigDecimal montant, String locationId, String referenceId);

    /**
     * Vérifie le statut d'un paiement Square
     * @param squarePaymentId l'ID du paiement Square
     * @return le statut du paiement (COMPLETED, FAILED, PENDING, etc.)
     */
    String verifierStatutPaiement(String squarePaymentId);

    /**
     * Récupère l'URL du reçu d'un paiement Square
     * @param squarePaymentId l'ID du paiement Square
     * @return l'URL du reçu
     */
    String recupererUrlRecu(String squarePaymentId);

    /**
     * Rembourse un paiement Square
     * @param squarePaymentId l'ID du paiement à rembourser
     * @param montant le montant à rembourser
     * @param raison la raison du remboursement
     * @return l'ID du remboursement Square
     */
    String rembourserPaiement(String squarePaymentId, BigDecimal montant, String raison);

    /**
     * Rembourse un paiement Square (surcharge sans raison)
     * @param squarePaymentId l'ID du paiement à rembourser
     * @param montant le montant à rembourser
     * @return l'ID du remboursement Square
     */
    default String refundPayment(String squarePaymentId, BigDecimal montant) {
        return rembourserPaiement(squarePaymentId, montant, "Remboursement demandé");
    }

    /**
     * Récupère les détails d'un paiement Square
     * @param squarePaymentId l'ID du paiement Square
     * @return un objet contenant les détails du paiement
     */
    SquarePaymentDetails recupererDetailsPaiement(String squarePaymentId);

    /**
     * Classe pour encapsuler les détails d'un paiement Square
     */
    record SquarePaymentDetails(
        String paymentId,
        String orderId,
        String status,
        BigDecimal montant,
        String receiptUrl,
        String locationId
    ) {}
}

