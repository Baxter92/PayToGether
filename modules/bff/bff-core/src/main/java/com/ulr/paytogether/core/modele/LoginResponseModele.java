package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modèle métier pour la réponse de login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseModele {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Integer expiresIn;
    private Integer refreshExpiresIn;
    private String scope;
}

