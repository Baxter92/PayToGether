package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.modele.ImageDealModele;

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
    DealModele mettreAJourStatut(UUID uuid, StatutDeal statut);

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

    /**
     * Supprimer les images d'un deal dont les UUID ne sont pas dans la liste fournie
     */
    void supprimerImagesNonPresentes(UUID dealUuid, List<UUID> uuidsAConserver);

    /**
     * Ajouter une nouvelle image à un deal
     */
    ImageDealModele ajouterImage(UUID dealUuid, ImageDealModele image);

    /**
     * Mettre à jour une image existante (statut, isPrincipal)
     */
    ImageDealModele mettreAJourImageExistante(UUID dealUuid, UUID imageUuid, ImageDealModele image);

    /**
     * Récupérer une image par son UUID et celui du deal
     */
    Optional<ImageDealModele> trouverImageParUuid(UUID dealUuid, UUID imageUuid);
}
