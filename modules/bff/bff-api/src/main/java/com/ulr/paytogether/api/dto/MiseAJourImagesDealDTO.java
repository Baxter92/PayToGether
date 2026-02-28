package com.ulr.paytogether.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la mise à jour des images d'un deal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiseAJourImagesDealDTO {

    @NotEmpty(message = "Au moins une image est obligatoire")
    @Valid
    private List<ImageDealDto> listeImages;
}

