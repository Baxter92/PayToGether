package com.ulr.paytogether.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ulr.paytogether.core.enumeration.MethodePaiement;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour représenter un paiement dans la liste admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementListDTO {

    private UUID uuid;

    // Informations client
    private UUID clientUuid;
    private String clientNom;
    private String clientPrenom;
    private String clientEmail;

    // Informations commande
    private UUID commandeUuid;
    private String numeroCommande;

    // Informations deal
    private UUID dealUuid;
    private String dealTitre;

    // Informations marchand
    private UUID marchandUuid;
    private String marchandNom;
    private String marchandPrenom;
    private String marchandEmail;

    // Détails paiement
    private BigDecimal montant;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datePaiement;

    private Integer nombreDePart;
    private MethodePaiement methodePaiement;
    private StatutPaiement statutPaiement;
}

