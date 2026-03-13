package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO de réponse après validation de factures clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationFacturesClientResponseDTO {
    private UUID commandeUuid;
    private String numeroCommande;
    private Integer nombreValidations;
    private Integer nombreTotal;
    private Boolean toutesValidees;
    private String message;
}

