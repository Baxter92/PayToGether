package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.MethodePaiement;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour l'entité Paiement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementDTO {

    private UUID uuid;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "Le statut est obligatoire")
    private StatutPaiement statut;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private MethodePaiement methodePaiement;

    private String transactionId;

    @NotNull(message = "L'UUID de l'utilisateur est obligatoire")
    private UUID utilisateurUuid;

    private String utilisateurNom;
    private String utilisateurPrenom;

    @NotNull(message = "L'UUID de la commande est obligatoire")
    private UUID commandeUuid;

    private LocalDateTime datePaiement;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;
}
