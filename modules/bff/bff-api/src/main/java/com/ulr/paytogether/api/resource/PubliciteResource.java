package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.PubliciteApiAdapter;
import com.ulr.paytogether.api.dto.PubliciteDTO;
import com.ulr.paytogether.core.enumeration.StatutImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour les publicités
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/publicites")
@RequiredArgsConstructor
@Slf4j
public class PubliciteResource {

    private final PubliciteApiAdapter publiciteApiAdapter;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PubliciteDTO> creer(@Valid @RequestBody PubliciteDTO publiciteDTO) {
        log.info("Création d'une publicité: {}", publiciteDTO.getTitre());
        PubliciteDTO resultat = publiciteApiAdapter.creer(publiciteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultat);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PubliciteDTO> lireParUuid(@PathVariable UUID uuid) {
        log.debug("Lecture de la publicité: {}", uuid);
        return publiciteApiAdapter.trouverParUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PubliciteDTO>> lireTous() {
        log.debug("Lecture de toutes les publicités");
        List<PubliciteDTO> publicites = publiciteApiAdapter.trouverTous();
        return ResponseEntity.ok(publicites);
    }

    /**
     * Récupérer les publicités actives (PUBLIC)
     */
    @GetMapping("/actives")
    public ResponseEntity<List<PubliciteDTO>> lireActives() {
        log.debug("Lecture des publicités actives");
        List<PubliciteDTO> publicites = publiciteApiAdapter.trouverActives();
        return ResponseEntity.ok(publicites);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{uuid}")
    public ResponseEntity<PubliciteDTO> mettreAJour(
            @PathVariable UUID uuid,
            @RequestBody PubliciteDTO publiciteDTO) {
        log.info("Mise à jour de la publicité: {}", uuid);
        PubliciteDTO resultat = publiciteApiAdapter.mettreAJour(uuid, publiciteDTO);
        return ResponseEntity.ok(resultat);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID uuid) {
        log.info("Suppression de la publicité: {}", uuid);
        publiciteApiAdapter.supprimerParUuid(uuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirmer l'upload d'une image de publicité
     * Endpoint appelé par le frontend après upload réussi vers MinIO
     */
    @PatchMapping("/{publiciteUuid}/images/{imageUuid}/confirm")
    public ResponseEntity<Void> confirmerUploadImage(
            @PathVariable UUID publiciteUuid,
            @PathVariable UUID imageUuid) {
        log.info("Confirmation upload image {} pour publicité {}", imageUuid, publiciteUuid);

        try {
            publiciteApiAdapter.mettreAJourStatutImage(
                publiciteUuid,
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
     * Obtenir l'URL de lecture d'une image de publicité (PUBLIC)
     * Génère une URL présignée pour lire l'image depuis MinIO
     */
    @GetMapping("/{publiciteUuid}/images/{imageUuid}/url")
    public ResponseEntity<java.util.Map<String, String>> obtenirUrlImage(
            @PathVariable UUID publiciteUuid,
            @PathVariable UUID imageUuid) {
        log.debug("Récupération de l'URL de lecture pour l'image {} de la publicité {}", imageUuid, publiciteUuid);

        try {
            String urlLecture = publiciteApiAdapter.obtenirUrlLectureImage(publiciteUuid, imageUuid);
            return ResponseEntity.ok(java.util.Map.of("url", urlLecture));
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la récupération de l'URL de lecture: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
