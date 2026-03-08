package com.ulr.paytogether.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO pour créer un paiement Square.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreerPaiementSquareDTO {

    @NotNull(message = "L'UUID du deal est obligatoire")
    private UUID dealUuid;

    @NotNull(message = "L'UUID de l'utilisateur est obligatoire")
    private UUID utilisateurUuid;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotBlank(message = "Le token Square est obligatoire")
    private String squareToken;

    @NotBlank(message = "La méthode de paiement est obligatoire")
    private String methodePaiement; // SQUARE_CARD, SQUARE_GOOGLE_PAY, SQUARE_APPLE_PAY, SQUARE_CASH_APP_PAY

    private String locationId; // Optionnel, utilisera la valeur par défaut si non fourni

    private int nombreDePart; // Optionnel, nombre de parts achetées (par défaut 1)

    private AdresseDTO adresse;
}

