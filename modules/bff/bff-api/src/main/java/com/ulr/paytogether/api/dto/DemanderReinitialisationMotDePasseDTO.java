package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour demander la réinitialisation du mot de passe
 * L'utilisateur fournit uniquement son email
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemanderReinitialisationMotDePasseDTO {

    private String email;
}

