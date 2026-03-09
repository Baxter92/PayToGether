package com.ulr.paytogether.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour représenter un participant à un deal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealParticipantDTO {

    private UUID dealUuid;
    private UUID utilisateurUuid;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private Integer nombreDePart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateParticipation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateModification;

    // Informations de paiement
    private BigDecimal montantTotal;        // Montant total payé (nombreDePart × prixPart)
    private StatutPaiement statutPaiement;  // Statut du paiement (CONFIRME, EN_ATTENTE, ECHOUE, etc.)

    // Adresse de paiement/livraison
    private String adresseRue;
    private String adresseVille;
    private String adresseProvince;
    private String adresseCodePostal;
    private String adressePays;
    private String adresseAppartement;
    private String adresseNumeroPhone;
}

