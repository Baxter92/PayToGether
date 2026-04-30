package com.ulr.paytogether.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * DTO pour représenter une commande dans la liste admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeListDTO {

    private UUID uuid;
    private String numeroCommande;

    // Informations marchand
    private UUID marchandUuid;
    private String marchandNom;
    private String marchandPrenom;
    private String marchandEmail;

    // Informations deal
    private UUID dealUuid;
    private String dealTitre;

    // Détails commande
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;

    private BigDecimal montantTotalPaiements;
    private StatutCommande statut;

    // Champs flux payout / facturation
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDepotPayout;

    private String factureMarchandUrl;
}

