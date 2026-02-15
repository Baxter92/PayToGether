package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutImage;

import java.util.UUID;

public record ImageDealDto(
        UUID imageUuid,
        String urlImage,
        Boolean isPrincipal,
        String presignUrl,
        StatutImage statut
) {
}
