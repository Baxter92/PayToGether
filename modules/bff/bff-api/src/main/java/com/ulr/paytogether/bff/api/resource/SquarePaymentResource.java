package com.ulr.paytogether.bff.api.resource;

import com.ulr.paytogether.bff.api.apiadapter.SquarePaymentApiAdapter;
import com.ulr.paytogether.bff.api.dto.CreerPaiementSquareDTO;
import com.ulr.paytogether.bff.api.dto.PaiementSquareResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Crée un nouveau paiement Square.
     * Le frontend envoie le token Square généré par le SDK.
     *
     * POST /api/square-payments
     */
    @PostMapping
    public ResponseEntity<PaiementSquareResponseDTO> creerPaiementSquare(
             @RequestBody CreerPaiementSquareDTO dto) {
        log.info("POST /api/square-payments - Création paiement Square pour commande: {}",
                 dto.getCommandeUuid());

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
}

