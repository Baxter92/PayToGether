package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.DealApiAdapter;
import com.ulr.paytogether.api.dto.DealResponseDto;
import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resource REST pour la gestion des deals
 */
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealResource {

    private final DealApiAdapter dealApiAdapter;

    /**
     * Créer un nouveau deal
     */
    @PostMapping
    public ResponseEntity<DealResponseDto> creer(@RequestBody DealDTO dto) {
        log.info("Création d'un deal: {}", dto.getTitre());
        DealResponseDto deal = dealApiAdapter.creerDeal(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deal);
    }

    /**
     * Récupérer un deal par son UUID
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
    @GetMapping("/createur/{createurUuid}")
    public ResponseEntity<List<DealResponseDto>> lireParCreateur(@PathVariable UUID createurUuid) {
        log.debug("Récupération des deals du créateur: {}", createurUuid);
        return ResponseEntity.ok(dealApiAdapter.lireTousByCreateurUuid(createurUuid));
    }

    /**
     * Récupérer les deals d'une catégorie
     */
    @GetMapping("/categorie/{categorieUuid}")
    public ResponseEntity<List<DealResponseDto>> lireParCategorie(@PathVariable UUID categorieUuid) {
        log.debug("Récupération des deals de la catégorie: {}", categorieUuid);
        return ResponseEntity.ok(dealApiAdapter.lireTousByCategorieUuid(categorieUuid));
    }

    /**
     * Mettre à jour un deal
     */
    @PutMapping("/{uuid}")
    public ResponseEntity<DealResponseDto> mettreAJour(
            @PathVariable UUID uuid,
            @Valid @RequestBody DealDTO dto) {
        log.info("Mise à jour du deal: {}", uuid);
        return ResponseEntity.ok(dealApiAdapter.mettreAJour(uuid, dto));
    }

    /**
     * Supprimer un deal
     */
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
}
