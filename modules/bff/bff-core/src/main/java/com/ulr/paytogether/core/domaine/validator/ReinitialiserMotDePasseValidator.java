package com.ulr.paytogether.core.domaine.validator;

import org.springframework.stereotype.Component;

/**
 * Validator pour la réinitialisation de mot de passe
 */
@Component
public class ReinitialiserMotDePasseValidator {

    /**
     * Valide les données de réinitialisation de mot de passe
     */
    public void valider(String token, String nouveauMotDePasse) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Le token est obligatoire");
        }

        if (nouveauMotDePasse == null || nouveauMotDePasse.isBlank()) {
            throw new IllegalArgumentException("Le nouveau mot de passe est obligatoire");
        }

        if (nouveauMotDePasse.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

       /* // Règles de complexité optionnelles
        if (!nouveauMotDePasse.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une majuscule");
        }

        if (!nouveauMotDePasse.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une minuscule");
        }

        if (!nouveauMotDePasse.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre");
        }*/
    }
}

