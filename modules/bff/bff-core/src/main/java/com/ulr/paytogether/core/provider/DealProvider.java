package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.core.modele.PageModele;

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

    /**
     * Trouver tous les deals avec pagination
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de deals
     */
    PageModele<DealModele> trouverTous(int page, int size);

    List<DealModele> trouverParStatut(StatutDeal statut);

    /**
     * Trouver les deals par statut avec pagination
     * @param statut Statut du deal
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de deals
     */
    PageModele<DealModele> trouverParStatut(StatutDeal statut, int page, int size);

    List<DealModele> trouverParCreateur(UUID createurUuid);

    /**
     * Trouver les deals par créateur avec pagination
     * @param createurUuid UUID du créateur
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de deals
     */
    PageModele<DealModele> trouverParCreateur(UUID createurUuid, int page, int size);

    List<DealModele> trouverParCategorie(UUID categorieUuid);

    /**
     * Trouver les deals par catégorie avec pagination
     * @param categorieUuid UUID de la catégorie
     * @param page Numéro de la page (commence à 0)
     * @param size Taille de la page
     * @return Page de deals
     */
    PageModele<DealModele> trouverParCategorie(UUID categorieUuid, int page, int size);

    DealModele mettreAJour(UUID uuid, DealModele deal);

    void supprimerParUuid(UUID uuid);

    /**
     * Mettre à jour uniquement le statut d'un deal
     */
    DealModele mettreAJourStatut(UUID uuid, StatutDeal statut);

    /**
     * Basculer le statut favoris d'un deal (true <-> false)
     */
    DealModele basculerFavoris(UUID uuid);

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

    /**
     * Calculer la moyenne des notes des commentaires pour un deal
     * @param dealUuid UUID du deal
     * @return Moyenne des notes (null si aucun commentaire)
     */
    Double calculerMoyenneCommentaires(UUID dealUuid);

    /**
     * Compter le nombre réel de participants pour un deal
     * @param dealUuid UUID du deal
     * @return Nombre de participants
     */
    Long compterParticipantsReels(UUID dealUuid);

    /**
     * Calculer le nombre total de parts achetées pour un deal
     * @param dealUuid UUID du deal
     * @return Nombre total de parts achetées
     */
    Long calculerNombrePartsAchetees(UUID dealUuid);
}
