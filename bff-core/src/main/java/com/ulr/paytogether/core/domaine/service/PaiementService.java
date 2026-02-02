package com.ulr.paytogether.core.domaine.service;


import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutPaiement;

import java.util.List;
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
     * Mettre à jour un paiement
     */
    DealModele mettreAJour(UUID uuid, PaiementModele paiement);

    /**
     * Supprimer un paiement par son UUID
     */
    void supprimerParUuid(UUID uuid);
}