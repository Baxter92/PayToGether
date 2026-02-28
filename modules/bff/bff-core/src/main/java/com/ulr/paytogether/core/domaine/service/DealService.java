package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.enumeration.StatutDeal;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
     * Mettre à jour uniquement le statut d'un deal
     */
    DealModele mettreAJourStatut(UUID uuid, StatutDeal statut);

    /**
     * Mettre à jour uniquement les images d'un deal
     */
    DealModele mettreAJourImages(UUID uuid, DealModele deal);

    /**
     * Supprimer un deal par son UUID
     */
    void supprimerParUuid(UUID uuid);

    /**
     * Récupérer les villes disponibles pour les deals
     */
    Set<String> lireVillesDisponibles();

    /**
     * Mettre à jour le statut d'une image d'un deal
     */
    void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut);

    /**
     * Obtenir l'URL de lecture d'une image d'un deal
     */
    String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid);
}
