package com.ulr.paytogether.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'activation/désactivation d'un utilisateur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiverUtilisateurDTO {

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean actif;
}

