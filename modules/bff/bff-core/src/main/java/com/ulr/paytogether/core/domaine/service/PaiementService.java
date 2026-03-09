package com.ulr.paytogether.core.domaine.service;


import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.enumeration.StatutPaiement;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface du service Paiement
 */
public interface PaiementService {

    /**
     * Créer un paiement
     */
    PaiementModele creer(PaiementModele deal);

    /**
     * Lire un paiement par son UUID
     */
    Optional<PaiementModele> lireParUuid(UUID uuid);

    /**
     * Lire tous les paiements
     */
    List<PaiementModele> lireTous();

    /**
     * Lire les paiements par statut
     */
    List<PaiementModele> lireParStatut(StatutPaiement statut);

    /**
     * Lire les paiements d'un deal
     */
    List<PaiementModele> lireParDeal(UUID dealUuid);

    /**
     * Trouver les paiements par commande
     */
    List<PaiementModele> trouverParCommande(UUID commandeUuid);

    /**
     * Mettre à jour un paiement
     */
    DealModele mettreAJour(UUID uuid, PaiementModele paiement);

    /**
     * Supprimer un paiement par son UUID
     */
    void supprimerParUuid(UUID uuid);

    /**
     * Lire tous les paiements avec informations complètes (pour l'admin)
     */
    List<PaiementModele> lireTousAvecInfosCompletes();

    /**
     * Calculer les statistiques des paiements (pour l'admin)
     */
    Map<String, Object> calculerStatistiques();
}

