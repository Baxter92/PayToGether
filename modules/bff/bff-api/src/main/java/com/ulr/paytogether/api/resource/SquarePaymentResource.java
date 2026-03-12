package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.SquarePaymentApiAdapter;
import com.ulr.paytogether.api.dto.CreerPaiementSquareDTO;
import com.ulr.paytogether.api.dto.PaiementSquareResponseDTO;
import com.ulr.paytogether.api.dto.RemboursementEnMasseDTO;
import com.ulr.paytogether.api.dto.RemboursementEnMasseResponseDTO;
import com.ulr.paytogether.core.domaine.service.SquarePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST pour les paiements Square.
 * Expose les endpoints pour gérer les paiements via Square Payment API.
 */
@RestController
@RequestMapping("/api/square-payments")
@RequiredArgsConstructor
@Slf4j
public class SquarePaymentResource {

    private final SquarePaymentApiAdapter apiAdapter;
    private final SquarePaymentService squarePaymentService;

    /**
     * Crée un nouveau paiement Square.
     * Le frontend envoie le token Square généré par le SDK.
     *
     * POST /api/square-payments
     */
    @PostMapping
    public ResponseEntity<PaiementSquareResponseDTO> creerPaiementSquare(
             @RequestBody CreerPaiementSquareDTO dto) {
        log.info("POST /api/square-payments - Création paiement Square pour le deal: {}",
                 dto.getDealUuid());

        try {
            PaiementSquareResponseDTO response = apiAdapter.creerPaiementSquare(dto);
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        } catch (Exception e) {
            log.error("Erreur création paiement Square: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Vérifie le statut d'un paiement Square.
     *
     * GET /api/square-payments/{paiementUuid}/status
     */
    @GetMapping("/{paiementUuid}/status")
    public ResponseEntity<PaiementSquareResponseDTO> verifierStatutPaiement(
            @PathVariable UUID paiementUuid) {
        log.info("GET /api/square-payments/{}/status - Vérification statut", paiementUuid);

        try {
            PaiementSquareResponseDTO response = apiAdapter.verifierStatutPaiement(paiementUuid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur vérification statut paiement Square: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Rembourse un paiement Square.
     *
     * POST /api/square-payments/{paiementUuid}/refund
     */
    @PostMapping("/{paiementUuid}/refund")
    public ResponseEntity<PaiementSquareResponseDTO> rembourserPaiement(
            @PathVariable UUID paiementUuid) {
        log.info("POST /api/square-payments/{}/refund - Remboursement", paiementUuid);

        try {
            PaiementSquareResponseDTO response = apiAdapter.rembourserPaiement(paiementUuid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur remboursement paiement Square: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Rembourse plusieurs paiements en masse (ADMIN uniquement).
     * Pour chaque utilisateur :
     * - Rembourse son paiement via Square
     * - Envoie un email de confirmation
     * - Supprime sa participation au deal
     * - Supprime le paiement
     * - Supprime la commande si elle n'a plus de paiements
     *
     * POST /api/square-payments/refund-bulk
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/refund-bulk")
    public ResponseEntity<RemboursementEnMasseResponseDTO> rembourserPaiementsEnMasse(
            @Valid @RequestBody RemboursementEnMasseDTO dto) {
        log.info("POST /api/square-payments/refund-bulk - Remboursement en masse de {} utilisateurs pour le deal {}",
                dto.getUtilisateurUuids().size(), dto.getDealUuid());

        try {
            int nombreRemboursements = squarePaymentService.rembourserPaiementsEnMasse(
                    dto.getUtilisateurUuids(),
                    dto.getDealUuid(),
                    dto.getRaisonRemboursement()
            );

            int nombreEchecs = dto.getUtilisateurUuids().size() - nombreRemboursements;

            RemboursementEnMasseResponseDTO response = RemboursementEnMasseResponseDTO.builder()
                    .dealUuid(dto.getDealUuid())
                    .nombreUtilisateurs(dto.getUtilisateurUuids().size())
                    .nombreRemboursementsReussis(nombreRemboursements)
                    .nombreEchecs(nombreEchecs)
                    .message(String.format("%d/%d remboursements effectués avec succès",
                            nombreRemboursements, dto.getUtilisateurUuids().size()))
                    .details(new ArrayList<>())
                    .build();

            log.info("Remboursement en masse terminé: {}/{} succès",
                    nombreRemboursements, dto.getUtilisateurUuids().size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur remboursement en masse: {}", e.getMessage(), e);
            throw e;
        }
    }
}

