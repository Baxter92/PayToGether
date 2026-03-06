package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.PaiementModele;

import java.util.UUID;

/**
 * Interface du service métier pour les paiements Square.
 */
public interface SquarePaymentService {

    /**
     * Crée un paiement Square
     * @param paiement les données du paiement
     * @return le paiement créé avec les informations Square
     */
    PaiementModele creerPaiementSquare(PaiementModele paiement);

    /**
     * Traite un paiement Square de manière asynchrone
     * @param paiement les données du paiement
     * @return le paiement avec statut mis à jour
     */
    PaiementModele traiterPaiementSquare(PaiementModele paiement);

    /**
     * Vérifie le statut d'un paiement Square
     * @param paiementUuid l'UUID du paiement
     * @return le paiement avec statut à jour
     */
    PaiementModele verifierStatutPaiement(UUID paiementUuid);

    /**
     * Rembourse un paiement Square
     * @param paiementUuid l'UUID du paiement à rembourser
     * @return le paiement remboursé
     */
    PaiementModele rembourserPaiement(UUID paiementUuid);

    void mettreAJourStatutCommandeDeal(UUID paiementUuid, String statut);
}

