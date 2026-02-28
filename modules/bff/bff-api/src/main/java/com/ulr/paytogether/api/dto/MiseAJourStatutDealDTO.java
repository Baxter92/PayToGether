package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutDeal;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la mise à jour du statut d'un deal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiseAJourStatutDealDTO {

    @NotNull(message = "Le statut est obligatoire")
    private StatutDeal statut;
}

