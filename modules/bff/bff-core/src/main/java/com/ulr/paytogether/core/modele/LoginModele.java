package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modèle métier pour la requête de login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginModele {

    private String username;
    private String password;
}

