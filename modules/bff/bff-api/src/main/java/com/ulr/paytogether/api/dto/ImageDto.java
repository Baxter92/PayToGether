package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutImage;

import java.util.UUID;

public record ImageDto(
        UUID imageUuid,
        String urlImage,
        String presignUrl,
        StatutImage statut
) {
}
