package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CreerPaiementSquareDTO;
import com.ulr.paytogether.api.dto.PaiementSquareResponseDTO;
import com.ulr.paytogether.api.mapper.SquarePaymentMapper;
import com.ulr.paytogether.core.domaine.service.SquarePaymentService;
import com.ulr.paytogether.core.enumeration.MethodePaiement;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API Adapter pour les paiements Square.
 * Fait le pont entre les controllers REST et les services métier.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SquarePaymentApiAdapter {

    private final SquarePaymentService squarePaymentService;
    private final SquarePaymentMapper mapper;

    /**
     * Crée un paiement Square
     */
    public PaiementSquareResponseDTO creerPaiementSquare(CreerPaiementSquareDTO dto) {
        log.info("API Adapter: Creating Square payment for deal={}", dto.getDealUuid());
        // Appeler le service métier
        PaiementModele paiementCree = squarePaymentService.creerPaiementSquare(mapper.dtoVersModele(dto));

        // Convertir en DTO de réponse
        return mapper.modeleVersDto(paiementCree);
    }

    /**
     * Traite un paiement Square de manière asynchrone
     */
    public PaiementSquareResponseDTO traiterPaiementSquare(UUID paiementUuid) {
        log.info("API Adapter: Processing Square payment UUID={}", paiementUuid);

        // Cette méthode sera appelée par un handler asynchrone
        // Pour l'instant, on retourne juste le paiement
        PaiementModele paiement = squarePaymentService.verifierStatutPaiement(paiementUuid);
        return mapper.modeleVersDto(paiement);
    }

    /**
     * Vérifie le statut d'un paiement Square
     */
    public PaiementSquareResponseDTO verifierStatutPaiement(UUID paiementUuid) {
        log.info("API Adapter: Checking Square payment status UUID={}", paiementUuid);

        PaiementModele paiement = squarePaymentService.verifierStatutPaiement(paiementUuid);
        return mapper.modeleVersDto(paiement);
    }

    /**
     * Rembourse un paiement Square
     */
    public PaiementSquareResponseDTO rembourserPaiement(UUID paiementUuid) {
        log.info("API Adapter: Refunding Square payment UUID={}", paiementUuid);

        PaiementModele paiement = squarePaymentService.rembourserPaiement(paiementUuid);
        return mapper.modeleVersDto(paiement);
    }


}

