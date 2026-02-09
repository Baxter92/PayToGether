package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.PubliciteApiAdapter;
import com.ulr.paytogether.api.dto.PubliciteDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour les publicités
 */
@RestController
@RequestMapping("/api/publicites")
@RequiredArgsConstructor
@Slf4j
public class PubliciteResource {

    private final PubliciteApiAdapter publiciteApiAdapter;

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

    @GetMapping
    public ResponseEntity<List<PubliciteDTO>> lireTous() {
        log.debug("Lecture de toutes les publicités");
        List<PubliciteDTO> publicites = publiciteApiAdapter.trouverTous();
        return ResponseEntity.ok(publicites);
    }

    @GetMapping("/actives")
    public ResponseEntity<List<PubliciteDTO>> lireActives() {
        log.debug("Lecture des publicités actives");
        List<PubliciteDTO> publicites = publiciteApiAdapter.trouverActives();
        return ResponseEntity.ok(publicites);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<PubliciteDTO> mettreAJour(
            @PathVariable UUID uuid,
            @RequestBody PubliciteDTO publiciteDTO) {
        log.info("Mise à jour de la publicité: {}", uuid);
        PubliciteDTO resultat = publiciteApiAdapter.mettreAJour(uuid, publiciteDTO);
        return ResponseEntity.ok(resultat);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID uuid) {
        log.info("Suppression de la publicité: {}", uuid);
        publiciteApiAdapter.supprimerParUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}
