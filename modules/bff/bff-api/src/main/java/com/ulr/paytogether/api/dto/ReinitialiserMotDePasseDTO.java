package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réinitialisation du mot de passe d'un utilisateur
 * Validation gérée par ReinitialiserMotDePasseValidator (bff-core)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinitialiserMotDePasseDTO {

    private String token;
    private String nouveauMotDePasse;
}

