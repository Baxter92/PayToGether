package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.PaiementModele;

import java.io.IOException;

/**
 * Service pour générer des factures au format PDF
 */
public interface InvoiceGeneratorService {
    
    /**
     * Génère une facture PDF pour un client à partir d'un paiement
     * 
     * @param paiement Paiement du client
     * @param numeroCommande Numéro de la commande
     * @param dealTitre Titre du deal
     * @param isHomeDelivery Si c'est une livraison à domicile ou pickup
     * @param adresseLivraison Adresse de livraison/pickup
     * @return Contenu du PDF sous forme de byte array
     * @throws IOException Si une erreur survient lors de la génération
     */
    byte[] genererFactureClient(
        PaiementModele paiement,
        String numeroCommande,
        String dealTitre,
        boolean isHomeDelivery,
        String adresseLivraison
    ) throws IOException;
}

