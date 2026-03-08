package com.ulr.paytogether.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour l'entité Adresse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdresseDTO {

    private UUID uuid;

    @NotBlank(message = "La rue est obligatoire")
    @Size(max = 255, message = "La rue ne doit pas dépasser 255 caractères")
    private String rue;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
    private String ville;

    @NotBlank(message = "Le code postal est obligatoire")
    @Size(max = 20, message = "Le code postal ne doit pas dépasser 20 caractères")
    private String codePostal;

    private String numeroPhone;

    private String Appartement;

    private boolean homeDelivery;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;
}
