package com.ulr.paytogether.bff.api.apiadapter;

import com.ulr.paytogether.bff.api.dto.CreerPaiementSquareDTO;
import com.ulr.paytogether.bff.api.dto.PaiementSquareResponseDTO;
import com.ulr.paytogether.core.domaine.service.SquarePaymentService;
import com.ulr.paytogether.core.enumeration.MethodePaiement;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API Adapter pour les paiements Square.
 * Fait le pont entre les controllers REST et les services métier.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SquarePaymentApiAdapter {

    private final SquarePaymentService squarePaymentService;

    /**
     * Crée un paiement Square
     */
    public PaiementSquareResponseDTO creerPaiementSquare(CreerPaiementSquareDTO dto) {
        log.info("API Adapter: Creating Square payment for commande={}", dto.getCommandeUuid());

        // Créer le modèle métier
        PaiementModele paiement = PaiementModele.builder()
            .montant(dto.getMontant())
            .statut(StatutPaiement.EN_ATTENTE)
            .methodePaiement(MethodePaiement.valueOf(dto.getMethodePaiement()))
            .squareToken(dto.getSquareToken())
            .squareLocationId(dto.getLocationId())
            .utilisateur(UtilisateurModele.builder().uuid(dto.getUtilisateurUuid()).build())
            .commande(CommandeModele.builder().uuid(dto.getCommandeUuid()).build())
            .datePaiement(LocalDateTime.now())
            .build();

        // Appeler le service métier
        PaiementModele paiementCree = squarePaymentService.creerPaiementSquare(paiement);

        // Convertir en DTO de réponse
        return modeleVersDto(paiementCree);
    }

    /**
     * Traite un paiement Square de manière asynchrone
     */
    public PaiementSquareResponseDTO traiterPaiementSquare(UUID paiementUuid) {
        log.info("API Adapter: Processing Square payment UUID={}", paiementUuid);

        // Cette méthode sera appelée par un handler asynchrone
        // Pour l'instant, on retourne juste le paiement
        PaiementModele paiement = squarePaymentService.verifierStatutPaiement(paiementUuid);
        return modeleVersDto(paiement);
    }

    /**
     * Vérifie le statut d'un paiement Square
     */
    public PaiementSquareResponseDTO verifierStatutPaiement(UUID paiementUuid) {
        log.info("API Adapter: Checking Square payment status UUID={}", paiementUuid);

        PaiementModele paiement = squarePaymentService.verifierStatutPaiement(paiementUuid);
        return modeleVersDto(paiement);
    }

    /**
     * Rembourse un paiement Square
     */
    public PaiementSquareResponseDTO rembourserPaiement(UUID paiementUuid) {
        log.info("API Adapter: Refunding Square payment UUID={}", paiementUuid);

        PaiementModele paiement = squarePaymentService.rembourserPaiement(paiementUuid);
        return modeleVersDto(paiement);
    }

    /**
     * Convertit un modèle métier en DTO de réponse
     */
    private PaiementSquareResponseDTO modeleVersDto(PaiementModele modele) {
        return PaiementSquareResponseDTO.builder()
            .uuid(modele.getUuid())
            .montant(modele.getMontant())
            .statut(modele.getStatut().name())
            .methodePaiement(modele.getMethodePaiement().name())
            .transactionId(modele.getTransactionId())
            .squarePaymentId(modele.getSquarePaymentId())
            .squareOrderId(modele.getSquareOrderId())
            .squareLocationId(modele.getSquareLocationId())
            .squareReceiptUrl(modele.getSquareReceiptUrl())
            .messageErreur(modele.getMessageErreur())
            .utilisateurUuid(modele.getUtilisateur() != null ? modele.getUtilisateur().getUuid() : null)
            .commandeUuid(modele.getCommande() != null ? modele.getCommande().getUuid() : null)
            .datePaiement(modele.getDatePaiement())
            .dateCreation(modele.getDateCreation())
            .dateModification(modele.getDateModification())
            .build();
    }
}

