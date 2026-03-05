package com.ulr.paytogether.core.enumeration;

/**
 * Énumération des statuts de paiement
 * Support des statuts Square Payment
 */
public enum StatutPaiement {
    EN_ATTENTE,     // En attente de traitement
    PROCESSING,     // En cours de traitement (Square)
    CONFIRME,       // Paiement confirmé et complété
    ECHOUE,         // Échec du paiement
    REFUNDED,       // Remboursé
    CANCELLED       // Annulé
}
