package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutImage;

public record ImageDealDto(
        String urlImage,
        Boolean isPrincipal,
        String presignUrl,
        StatutImage statut
) {
}
