package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.enumeration.StatutDeal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les deals
 */
public interface DealProvider {

    DealModele sauvegarder(DealModele deal);

    Optional<DealModele> trouverParUuid(UUID uuid);

    List<DealModele> trouverTous();

    List<DealModele> trouverParStatut(StatutDeal statut);

    List<DealModele> trouverParCreateur(UUID createurUuid);

    List<DealModele> trouverParCategorie(UUID categorieUuid);

    DealModele mettreAJour(UUID uuid, DealModele deal);

    void supprimerParUuid(UUID uuid);

    /**
     * Mettre à jour uniquement le statut d'un deal
     */
    DealModele mettreAJourStatut(UUID uuid, com.ulr.paytogether.core.enumeration.StatutDeal statut);

    /**
     * Mettre à jour uniquement les images d'un deal
     */
    DealModele mettreAJourImages(UUID uuid, DealModele deal);

    /**
     * Mettre à jour le statut d'une image d'un deal
     */
    void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut);

    /**
     * Obtenir l'URL de lecture d'une image d'un deal
     */
    String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid);
}
