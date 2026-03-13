package com.ulr.paytogether.core.enumeration;

/**
 * Énumération des statuts de commande
 */
public enum StatutCommande {
    EN_COURS,           // Deal créé, paiements en cours
    COMPLETEE,          // Tous les paiements reçus
    PAYOUT,             // Payout vers le vendeur effectué
    INVOICE_SELLER,     // Facture du vendeur reçue
    INVOICE_CUSTOMER,   // Factures clients générées et envoyées
    CONFIRMEE,
    ANNULEE,
    REMBOURSEE,
    FACTURE_MARCHAND_RECUE,
    FACTURES_CLIENT_ENVOYEES,
    TERMINEE            // Toutes les validations complètes
}
