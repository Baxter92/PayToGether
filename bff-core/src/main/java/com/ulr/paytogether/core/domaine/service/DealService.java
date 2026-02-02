package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutDeal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface du service Deal
 */
public interface DealService {

    /**
     * Créer un deal
     */
    DealModele creer(DealModele deal);

    /**
     * Lire un deal par son UUID
     */
    Optional<DealModele> lireParUuid(UUID uuid);

    /**
     * Lire tous les deals
     */
    List<DealModele> lireTous();

    /**
     * Lire les deals par statut
     */
    List<DealModele> lireParStatut(StatutDeal statut);

    /**
     * Lire les deals d'un créateur
     */
    List<DealModele> lireParCreateur(UUID createurUuid);

    /**
     * Lire les deals d'une catégorie
     */
    List<DealModele> lireParCategorie(UUID categorieUuid);

    /**
     * Mettre à jour un deal
     */
    DealModele mettreAJour(UUID uuid, DealModele deal);

    /**
     * Supprimer un deal par son UUID
     */
    void supprimerParUuid(UUID uuid);
}
