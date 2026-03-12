package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO de réponse pour le remboursement en masse.
 * Contient le nombre de remboursements réussis et les détails.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemboursementEnMasseResponseDTO {

    private UUID dealUuid;
    private int nombreUtilisateurs;
    private int nombreRemboursementsReussis;
    private int nombreEchecs;
    private String message;
    private List<String> details;
}

