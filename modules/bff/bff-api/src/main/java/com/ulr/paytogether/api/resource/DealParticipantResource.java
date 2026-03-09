package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.DealParticipantApiAdapter;
import com.ulr.paytogether.api.dto.DealParticipantDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Resource REST pour gérer les participants aux deals
 * Endpoints protégés pour les rôles ADMIN et VENDEUR uniquement
 */
@RestController
@RequestMapping("/api/deals/{dealUuid}/participants")
@RequiredArgsConstructor
@Slf4j
public class DealParticipantResource {

    private final DealParticipantApiAdapter dealParticipantApiAdapter;

    /**
     * Liste tous les participants d'un deal
     * Accessible uniquement aux ADMIN et VENDEUR
     *
     * @param dealUuid UUID du deal
     * @return Liste des participants avec leurs informations complètes
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<List<DealParticipantDTO>> listerParticipants(
            @PathVariable UUID dealUuid) {
        log.info("Récupération de la liste des participants du deal: {}", dealUuid);

        List<DealParticipantDTO> participants = dealParticipantApiAdapter.listerParticipantsDeal(dealUuid);

        log.info("Nombre de participants trouvés: {}", participants.size());
        return ResponseEntity.ok(participants);
    }
}

