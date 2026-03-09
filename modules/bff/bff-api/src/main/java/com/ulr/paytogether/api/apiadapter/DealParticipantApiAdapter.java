package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.DealParticipantDTO;
import com.ulr.paytogether.api.mapper.DealParticipantMapper;
import com.ulr.paytogether.core.domaine.service.DealParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptateur API pour gérer les participants aux deals
 * Couche API (bff-api) - Partie gauche de l'architecture hexagonale
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DealParticipantApiAdapter {

    private final DealParticipantService dealParticipantService;
    private final DealParticipantMapper mapper;

    /**
     * Récupère la liste des participants d'un deal avec leurs informations complètes
     * @param dealUuid UUID du deal
     * @return Liste des participants
     */
    public List<DealParticipantDTO> listerParticipantsDeal(UUID dealUuid) {
        log.debug("Récupération de la liste des participants pour le deal: {}", dealUuid);

        return dealParticipantService.trouverParticipantsParDealAvecUtilisateur(dealUuid).stream()
                .map(mapper::modeleVersDto)
                .toList();
    }
}

