package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.CategorieApiAdapter;
import com.ulr.paytogether.api.dto.CategorieDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour les catégories
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategorieResource {

    private final CategorieApiAdapter categorieApiAdapter;

    /**
     * Créer une nouvelle catégorie
     */
    @PostMapping
    public ResponseEntity<CategorieDTO> creer(@Valid @RequestBody CategorieDTO categorieDTO) {
        log.info("Création d'une catégorie: {}", categorieDTO.getNom());

        try {
            CategorieDTO resultat = categorieApiAdapter.creer(categorieDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultat);
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la création de la catégorie: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer une catégorie par son UUID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<CategorieDTO> lireParUuid(@PathVariable UUID uuid) {
        log.debug("Lecture de la catégorie: {}", uuid);
        return categorieApiAdapter.trouverParUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer une catégorie par son nom
     */
    @GetMapping("/nom/{nom}")
    public ResponseEntity<CategorieDTO> lireParNom(@PathVariable String nom) {
        log.debug("Lecture de la catégorie par nom: {}", nom);
        return categorieApiAdapter.trouverParNom(nom)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer toutes les catégories
     */
    @GetMapping
    public ResponseEntity<List<CategorieDTO>> lireTous() {
        log.debug("Récupération de toutes les catégories");
        List<CategorieDTO> categories = categorieApiAdapter.trouverTous();
        return ResponseEntity.ok(categories);
    }

    /**
     * Vérifier si une catégorie existe par son nom
     */
    @GetMapping("/existe/{nom}")
    public ResponseEntity<Boolean> existeParNom(@PathVariable String nom) {
        log.debug("Vérification de l'existence de la catégorie: {}", nom);
        boolean existe = categorieApiAdapter.existeParNom(nom);
        return ResponseEntity.ok(existe);
    }

    /**
     * Mettre à jour une catégorie
     */
    @PutMapping("/{uuid}")
    public ResponseEntity<CategorieDTO> mettreAJour(
            @PathVariable UUID uuid,
            @Valid @RequestBody CategorieDTO categorieDTO) {
        log.info("Mise à jour de la catégorie: {}", uuid);

        try {
            CategorieDTO resultat = categorieApiAdapter.mettreAJour(uuid, categorieDTO);
            return ResponseEntity.ok(resultat);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la mise à jour de la catégorie: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer une catégorie
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID uuid) {
        log.info("Suppression de la catégorie: {}", uuid);
        categorieApiAdapter.supprimerParUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}
