package com.ulr.paytogether.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
 * DTO pour la mise à jour partielle d'un deal
 * Note: Si listeImages est présente, elle sera traitée intelligemment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiseAJourDealDTO {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    @NotNull(message = "Le prix du deal est obligatoire")
    @Positive(message = "Le prix du deal doit être positif")
    private BigDecimal prixDeal;

    @NotNull(message = "Le prix par part est obligatoire")
    @Positive(message = "Le prix par part doit être positif")
    private BigDecimal prixPart;

    @NotNull(message = "Le nombre de participants est obligatoire")
    @Positive(message = "Le nombre de participants doit être positif")
    private Integer nbParticipants;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    @NotNull(message = "Le createur est obligatoire")
    private UUID createurUuid;

    @NotNull(message = "La catégorie est obligatoire")
    private UUID categorieUuid;

    private List<String> listePointsForts;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateExpiration;

    @NotNull(message = "La ville est obligatoire")
    private String ville;

    private String pays;

    /**
     * Liste d'images facultative
     * Si présente : gestion intelligente (ajout/modification/suppression)
     * Si absente : les images ne sont pas modifiées
     */
    @Valid
    private List<ImageDealDto> listeImages;
}

