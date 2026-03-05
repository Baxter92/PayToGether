package com.ulr.paytogether.core.domaine.validator;

import org.springframework.stereotype.Component;

/**
 * Validator pour l'activation de compte
 */
@Component
public class ActivationCompteValidator {

    /**
     * Valide le token d'activation
     */
    public void valider(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Le token d'activation est obligatoire");
        }

        // Validation du format du token (UUID sans tirets, 32 caractères)
        if (!token.matches("^[a-f0-9]{32}$")) {
            throw new IllegalArgumentException("Format de token invalide");
        }
    }
}

