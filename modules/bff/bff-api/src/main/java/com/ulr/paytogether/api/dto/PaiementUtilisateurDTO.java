package com.ulr.paytogether.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour les paiements d'un utilisateur
 * Contient toutes les informations du paiement, de la commande, du deal et de l'adresse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementUtilisateurDTO {

    // Informations du paiement
    private UUID paiementUuid;
    private BigDecimal montantPaiement;
    private String statutPaiement;
    private String methodePaiement;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datePaiement;
    private String transactionId;

    // Informations de la commande
    private UUID commandeUuid;
    private String numeroCommande;
    private String statutCommande;
    private BigDecimal montantTotal;
    private Integer nbPartsAchetees;

    // Informations du deal
    private DealInfoDTO deal;

    // Informations de la catégorie
    private CategorieInfoDTO categorie;

    // Adresse de facturation
    private AdresseFacturationDTO adresseFacturation;

    /**
     * DTO imbriqué pour les informations du deal
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DealInfoDTO {
        private UUID uuid;
        private String titre;
        private String description;
        private String imagePrincipaleUrlPresignee;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime dateExpiration;
        private BigDecimal prixDeal;
        private BigDecimal prixPart;
    }

    /**
     * DTO imbriqué pour les informations de la catégorie
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorieInfoDTO {
        private UUID uuid;
        private String nom;
        private String icone;
    }

    /**
     * DTO imbriqué pour l'adresse de facturation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdresseFacturationDTO {
        private UUID uuid;
        private String rue;
        private String ville;
        private String codePostal;
        private String telephone;
        private String pays;
    }
}

