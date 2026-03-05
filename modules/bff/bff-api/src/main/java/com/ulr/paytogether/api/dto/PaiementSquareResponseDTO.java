package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un paiement Square.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementSquareResponseDTO {

    private UUID uuid;
    private BigDecimal montant;
    private String statut;
    private String methodePaiement;
    private String transactionId;

    // Champs spécifiques Square
    private String squarePaymentId;
    private String squareOrderId;
    private String squareLocationId;
    private String squareReceiptUrl;
    private String messageErreur;

    private UUID utilisateurUuid;
    private UUID commandeUuid;
    private LocalDateTime datePaiement;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

