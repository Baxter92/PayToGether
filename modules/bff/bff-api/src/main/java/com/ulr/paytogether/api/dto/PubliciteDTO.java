package com.ulr.paytogether.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour la création/modification d'une publicité
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PubliciteDTO {

    private UUID uuid;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    @NotBlank(message = "La description est obligatoire")
    private String description;

    private String lienExterne;

    @NotNull(message = "Les images sont obligatoires")
    @NotEmpty(message = "Au moins une image est obligatoire")
    private List<ImageDto> listeImages;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    private Boolean active;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;
}
