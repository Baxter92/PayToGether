package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour l'entité Commande
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeDTO {

    private UUID uuid;

    @NotNull(message = "Le montant total est obligatoire")
    @Positive(message = "Le montant total doit être positif")
    private BigDecimal montantTotal;

    @NotNull(message = "Le statut est obligatoire")
    private StatutCommande statut;

    @NotNull(message = "L'UUID de l'utilisateur est obligatoire")
    private UUID utilisateurUuid;

    private String utilisateurNom;
    private String utilisateurPrenom;

    @NotNull(message = "L'UUID du deal est obligatoire")
    private UUID dealUuid;

    private String dealTitre;
    private BigDecimal dealPrixPart;

    private List<PaiementDTO> paiements;

    private LocalDateTime dateCommande;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;
}
