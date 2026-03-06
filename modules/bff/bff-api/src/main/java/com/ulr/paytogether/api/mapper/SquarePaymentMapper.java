package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.CreerPaiementSquareDTO;
import com.ulr.paytogether.api.dto.PaiementSquareResponseDTO;
import com.ulr.paytogether.core.enumeration.MethodePaiement;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SquarePaymentMapper {

    private final CommandeMapper commandeMapper;

    public PaiementModele dtoVersModele(CreerPaiementSquareDTO dto) {
        if (dto == null) {
            return null;
        }

        return PaiementModele.builder()
                .montant(dto.getMontant())
                .statut(StatutPaiement.EN_ATTENTE)
                .methodePaiement(MethodePaiement.valueOf(dto.getMethodePaiement()))
                .squareToken(dto.getSquareToken())
                .squareLocationId(dto.getLocationId())
                .utilisateur(UtilisateurModele.builder().uuid(dto.getUtilisateurUuid()).build())
                .deal(DealModele.builder().uuid(dto.getDealUuid()).build())
                .datePaiement(LocalDateTime.now())
                .build();

    }

    /**
     * Convertit un modèle métier en DTO de réponse
     */
    public PaiementSquareResponseDTO modeleVersDto(PaiementModele modele) {
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
