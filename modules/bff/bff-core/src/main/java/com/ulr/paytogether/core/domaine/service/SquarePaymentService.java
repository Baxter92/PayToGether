package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.PaiementModele;

import java.util.List;
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

    /**
     * Rembourse plusieurs paiements en masse (admin uniquement)
     * Pour chaque utilisateur, rembourse son paiement, supprime sa participation et publie un événement
     *
     * @param utilisateurUuids Liste des UUIDs des utilisateurs à rembourser
     * @param dealUuid UUID du deal concerné
     * @param raisonRemboursement Raison du remboursement
     * @return Nombre de remboursements effectués avec succès
     */
    int rembourserPaiementsEnMasse(List<UUID> utilisateurUuids, UUID dealUuid, String raisonRemboursement);

    /**
     * Supprime la participation d'un utilisateur à un deal après remboursement
     * Supprime également le paiement et la commande si elle n'a plus de paiements
     *
     * @param utilisateurUuid UUID de l'utilisateur
     * @param dealUuid UUID du deal
     * @param nombreDeParts Nombre de parts à retirer
     */
    void supprimerParticipationApresRemboursement(UUID utilisateurUuid, UUID dealUuid, int nombreDeParts);

    /**
     * Met à jour le statut de la commande et du deal après paiement
     * @param paiementUuid UUID du paiement
     * @param statut Statut à appliquer
     * @param nombreDePart Nombre de parts achetées
     */
    void mettreAJourStatutCommandeDeal(UUID paiementUuid, String statut, int nombreDePart);
}

