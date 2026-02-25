package com.ulr.paytogether.api.dto;

import lombok.Builder;

@Builder
public record MettreUtilisateurDto(
        String nom,
        String prenom,
        String photoProfil
) {
}
