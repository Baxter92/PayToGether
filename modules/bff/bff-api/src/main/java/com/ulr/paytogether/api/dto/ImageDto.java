package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutImage;

public record ImageDto(
        String urlImage,
        String presignUrl,
        StatutImage statut
) {
}
