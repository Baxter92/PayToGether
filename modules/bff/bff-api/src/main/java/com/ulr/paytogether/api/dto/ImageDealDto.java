package com.ulr.paytogether.api.dto;

import com.ulr.paytogether.core.enumeration.StatutImage;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ImageDealDto(
        UUID imageUuid,
        String urlImage,
        Boolean isPrincipal,
        String presignUrl,
        StatutImage statut
) {
}
