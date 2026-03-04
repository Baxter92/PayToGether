package com.ulr.paytogether.core.enumeration;

/**
 * Énumération des méthodes de paiement
 * Support Square Payment : CARD, GOOGLE_PAY, APPLE_PAY, CASH_APP_PAY
 */
public enum MethodePaiement {
    CARTE_CREDIT,
    INTERAC,
    VIREMENT_BANCAIRE,
    SQUARE_CARD,        // Square - Paiement par carte
    SQUARE_GOOGLE_PAY,  // Square - Google Pay
    SQUARE_APPLE_PAY,   // Square - Apple Pay
    SQUARE_CASH_APP_PAY // Square - Cash App Pay
}
