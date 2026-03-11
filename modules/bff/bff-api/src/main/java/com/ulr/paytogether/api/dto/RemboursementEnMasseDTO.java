package com.ulr.paytogether.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO pour le remboursement en masse de paiements (admin uniquement).
 * Permet de rembourser plusieurs utilisateurs en une seule opération.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemboursementEnMasseDTO {

    @NotNull(message = "L'UUID du deal est obligatoire")
    private UUID dealUuid;

    @NotEmpty(message = "La liste des utilisateurs ne peut pas être vide")
    private List<UUID> utilisateurUuids;

    @Size(max = 500, message = "La raison ne peut pas dépasser 500 caractères")
    private String raisonRemboursement;
}

