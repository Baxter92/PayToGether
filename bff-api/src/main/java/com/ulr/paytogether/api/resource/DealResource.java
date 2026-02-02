package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.api.mapper.DealMapper;
import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutDeal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resource REST pour la gestion des deals
 */
@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
@Slf4j
public class DealResource {

    private final DealService dealService;
    private final DealMapper dealMapper;

    /**
     * Créer un nouveau deal
     */
    @PostMapping
    public ResponseEntity<DealDTO> creer(@Valid @RequestBody DealDTO dto) {
        log.info("Création d'un deal: {}", dto.getTitre());

        DealModele deal = dealMapper.versEntite(dto);
        DealModele created = dealService.creer(deal);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dealMapper.versDTO(created));
    }

    /**
     * Récupérer un deal par son UUID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<DealDTO> lireParUuid(@PathVariable UUID uuid) {
        log.debug("Récupération du deal: {}", uuid);

        return dealService.lireParUuid(uuid)
                .map(dealMapper::versDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer tous les deals
     */
    @GetMapping
    public ResponseEntity<List<DealDTO>> lireTous() {
        log.debug("Récupération de tous les deals");

        List<DealDTO> deals = dealService.lireTous()
                .stream()
                .map(dealMapper::versDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(deals);
    }

    /**
     * Récupérer les deals par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<DealDTO>> lireParStatut(@PathVariable StatutDeal statut) {
        log.debug("Récupération des deals avec le statut: {}", statut);

        List<DealDTO> deals = dealService.lireParStatut(statut)
                .stream()
                .map(dealMapper::versDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(deals);
    }

    /**
     * Récupérer les deals d'un créateur
     */
    @GetMapping("/createur/{createurUuid}")
    public ResponseEntity<List<DealDTO>> lireParCreateur(@PathVariable UUID createurUuid) {
        log.debug("Récupération des deals du créateur: {}", createurUuid);

        List<DealDTO> deals = dealService.lireParCreateur(createurUuid)
                .stream()
                .map(dealMapper::versDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(deals);
    }

    /**
     * Récupérer les deals d'une catégorie
     */
    @GetMapping("/categorie/{categorieUuid}")
    public ResponseEntity<List<DealDTO>> lireParCategorie(@PathVariable UUID categorieUuid) {
        log.debug("Récupération des deals de la catégorie: {}", categorieUuid);

        List<DealDTO> deals = dealService.lireParCategorie(categorieUuid)
                .stream()
                .map(dealMapper::versDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(deals);
    }

    /**
     * Mettre à jour un deal
     */
    @PutMapping("/{uuid}")
    public ResponseEntity<DealDTO> mettreAJour(
            @PathVariable UUID uuid,
            @Valid @RequestBody DealDTO dto) {
        log.info("Mise à jour du deal: {}", uuid);

        return dealService.lireParUuid(uuid)
                .map(deal -> {
                    dealMapper.mettreAJour(deal, dto);
                    DealModele updated = dealService.mettreAJour(uuid, deal);
                    return ResponseEntity.ok(dealMapper.versDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprimer un deal
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID uuid) {
        log.info("Suppression du deal: {}", uuid);

        if (dealService.lireParUuid(uuid).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        dealService.supprimerParUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}
