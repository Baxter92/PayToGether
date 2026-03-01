package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.CommentaireApiAdapter;
import com.ulr.paytogether.api.apiadapter.DealApiAdapter;
import com.ulr.paytogether.api.dto.*;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.enumeration.StatutImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Resource REST pour la gestion des deals
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealResource {

    private final DealApiAdapter dealApiAdapter;
    private final CommentaireApiAdapter commentaireApiAdapter;

    /**
     * Créer un nouveau deal
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    @PostMapping
    public ResponseEntity<DealResponseDto> creer(@RequestBody DealDTO dto) {
        log.info("Création d'un deal: {}", dto.getTitre());
        DealResponseDto deal = dealApiAdapter.creerDeal(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deal);
    }

    /**
     * Récupérer un deal par son UUID (PUBLIC)
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<DealResponseDto> lireParUuid(@PathVariable UUID uuid) {
        log.debug("Récupération du deal: {}", uuid);

        return dealApiAdapter.lireParUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer tous les deals
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DealResponseDto>> lireTous() {
        log.debug("Récupération de tous les deals");
        return ResponseEntity.ok(dealApiAdapter.lireTous());
    }

    /**
     * Récupérer les deals par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<DealResponseDto>> lireParStatut(@PathVariable StatutDeal statut) {
        log.debug("Récupération des deals avec le statut: {}", statut);
        return ResponseEntity.ok(dealApiAdapter.lireTousByStatut(statut));
    }

    /**
     * Récupérer les deals d'un créateur
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    @GetMapping("/createur/{createurUuid}")
    public ResponseEntity<List<DealResponseDto>> lireParCreateur(@PathVariable UUID createurUuid) {
        log.debug("Récupération des deals du créateur: {}", createurUuid);
        return ResponseEntity.ok(dealApiAdapter.lireTousByCreateurUuid(createurUuid));
    }

    /**
     * Récupérer les deals d'une catégorie (PUBLIC)
     */
    @GetMapping("/categorie/{categorieUuid}")
    public ResponseEntity<List<DealResponseDto>> lireParCategorie(@PathVariable UUID categorieUuid) {
        log.debug("Récupération des deals de la catégorie: {}", categorieUuid);
        return ResponseEntity.ok(dealApiAdapter.lireTousByCategorieUuid(categorieUuid));
    }

    /**
     * Mettre à jour un deal (sans statut et sans images)
     * Utiliser les endpoints PATCH dédiés pour mettre à jour le statut ou les images
     */
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<DealResponseDto> mettreAJour(
            @PathVariable UUID uuid,
            @RequestBody MiseAJourDealDTO dto) {
        log.info("Mise à jour des informations du deal: {}", uuid);
        return ResponseEntity.ok(dealApiAdapter.mettreAJour(uuid, dto));
    }

    /**
     * Mettre à jour uniquement le statut d'un deal
     */
    @PatchMapping("/{uuid}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<DealResponseDto> mettreAJourStatut(
            @PathVariable UUID uuid,
            @RequestBody MiseAJourStatutDealDTO dto) {
        log.info("Mise à jour du statut du deal {} vers {}", uuid, dto.getStatut());
        return ResponseEntity.ok(dealApiAdapter.mettreAJourStatut(uuid, dto.getStatut()));
    }

    /**
     * Mettre à jour uniquement les images d'un deal
     * Les anciennes images seront supprimées et remplacées par les nouvelles
     */
    @PatchMapping("/{uuid}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<DealResponseDto> mettreAJourImages(
            @PathVariable UUID uuid,
            @RequestBody MiseAJourImagesDealDTO dto) {
        log.info("Mise à jour des images du deal: {}", uuid);
        return ResponseEntity.ok(dealApiAdapter.mettreAJourImages(uuid, dto));
    }

    /**
     * Supprimer un deal
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID uuid) {
        log.info("Suppression du deal: {}", uuid);

        if (dealApiAdapter.lireParUuid(uuid).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        dealApiAdapter.supprimerParUuid(uuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer les villes disponibles pour les deals
     */
    @GetMapping("/villes")
    public ResponseEntity<Set<String>> lireVilles() {
        log.debug("Récupération des villes disponibles pour les deals");
        return ResponseEntity.ok(dealApiAdapter.lireVillesDisponibles());
    }

    /**
     * Confirmer l'upload d'une image
     * Endpoint appelé par le frontend après upload réussi vers MinIO
     */
    @PatchMapping("/{dealUuid}/images/{imageUuid}/confirm")
    public ResponseEntity<Void> confirmerUploadImage(
            @PathVariable UUID dealUuid,
            @PathVariable UUID imageUuid) {
        log.info("Confirmation upload image {} pour deal {}", imageUuid, dealUuid);

        try {
            dealApiAdapter.mettreAJourStatutImage(
                dealUuid,
                imageUuid,
                StatutImage.UPLOADED
            );
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la confirmation de l'upload: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Confirmer l'upload de plusieurs images en batch
     * Endpoint appelé par le frontend après upload réussi vers MinIO
     */
    @PatchMapping("/{dealUuid}/images/confirm-batch")
    public ResponseEntity<Void> confirmerUploadImagesBatch(
            @PathVariable UUID dealUuid,
            @RequestBody List<UUID> imageUuids) {

        imageUuids.forEach(imageUuid ->
                dealApiAdapter.mettreAJourStatutImage(dealUuid, imageUuid, StatutImage.UPLOADED)
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Obtenir l'URL de lecture d'une image (PUBLIC)
     * Génère une URL présignée pour lire l'image depuis MinIO
     */
    @GetMapping("/{dealUuid}/images/{imageUuid}/url")
    public ResponseEntity<Map<String, String>> obtenirUrlImage(
            @PathVariable UUID dealUuid,
            @PathVariable UUID imageUuid) {
        log.debug("Récupération de l'URL de lecture pour l'image {} du deal {}", imageUuid, dealUuid);

        try {
            String urlLecture = dealApiAdapter.obtenirUrlLectureImage(dealUuid, imageUuid);
            return ResponseEntity.ok(Map.of("url", urlLecture));
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la récupération de l'URL de lecture: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Récupérer tous les commentaires d'un deal (PUBLIC)
     */
    @GetMapping("/{dealUuid}/commentaires")
    public ResponseEntity<List<CommentaireDTO>> lireCommentaires(@PathVariable UUID dealUuid) {
        log.debug("Récupération des commentaires du deal: {}", dealUuid);
        List<CommentaireDTO> commentaires = commentaireApiAdapter.trouverParDeal(dealUuid);
        return ResponseEntity.ok(commentaires);
    }
}
