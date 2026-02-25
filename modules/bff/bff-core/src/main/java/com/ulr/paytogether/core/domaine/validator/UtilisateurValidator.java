package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Validator pour les entités Utilisateur
 * Utilise les règles de validation des DTOs UtilisateurDTO et CreerUtilisateurDTO
 */
@Component
public class UtilisateurValidator implements Validator {

    // Pattern pour valider le format email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Longueur minimale du mot de passe
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    public boolean supports(Class<?> clazz) {
        return UtilisateurModele.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UtilisateurModele utilisateur = (UtilisateurModele) target;

        // Validation de l'email (obligatoire et format valide)
        if (utilisateur.getEmail() == null || utilisateur.getEmail().isBlank()) {
            errors.rejectValue("email", "email.obligatoire", "L'attribut email est obligatoire");
        } else if (!EMAIL_PATTERN.matcher(utilisateur.getEmail()).matches()) {
            errors.rejectValue("email", "email.format", "L'email doit être valide");
        }

        // Validation du mot de passe (obligatoire)
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
            errors.rejectValue("motDePasse", "motDePasse.obligatoire", "L'attribut motDePasse est obligatoire");
        }

        // Validation du rôle (obligatoire)
        if (utilisateur.getRole() == null) {
            errors.rejectValue("role", "role.obligatoire", "L'attribut role est obligatoire");
        }
    }

    /**
     * Méthode de validation simplifiée qui lance des ValidationException
     * Utilisée dans les services métier
     *
     * @param utilisateur le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void valider(UtilisateurModele utilisateur) {
        if (utilisateur == null) {
            throw new ValidationException("utilisateur.null");
        }

        // Validation de l'email (obligatoire et format valide)
        if (utilisateur.getEmail() == null || utilisateur.getEmail().isBlank()) {
            throw new ValidationException("utilisateur.email.obligatoire");
        }
        if (!EMAIL_PATTERN.matcher(utilisateur.getEmail()).matches()) {
            throw new ValidationException("utilisateur.email.format");
        }

        // Validation du mot de passe (obligatoire)
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
            throw new ValidationException("utilisateur.motDePasse.obligatoire");
        }

        // Validation du rôle (obligatoire)
        if (utilisateur.getRole() == null) {
            throw new ValidationException("utilisateur.role.obligatoire");
        }
    }

    /**
     * Validation pour la création d'un utilisateur
     * Inclut les validations du nom et prénom (obligatoires pour CreerUtilisateurDTO)
     *
     * @param utilisateur le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourCreation(UtilisateurModele utilisateur) {
        if (utilisateur == null) {
            throw new ValidationException("utilisateur.null");
        }

        // Validation de l'email (obligatoire et format valide)
        if (utilisateur.getEmail() == null || utilisateur.getEmail().isBlank()) {
            throw new ValidationException("utilisateur.email.obligatoire");
        }
        if (!EMAIL_PATTERN.matcher(utilisateur.getEmail()).matches()) {
            throw new ValidationException("utilisateur.email.format");
        }

        // Validation du mot de passe (obligatoire pour la création)
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isBlank()) {
            throw new ValidationException("utilisateur.motDePasse.obligatoire");
        }

        // Validation optionnelle : mot de passe fort (minimum 8 caractères recommandé)
        if (utilisateur.getMotDePasse().length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("utilisateur.motDePasse.longueur", MIN_PASSWORD_LENGTH);
        }
    }

    /**
     * Validation pour la mise à jour d'un utilisateur
     *
     * @param utilisateur le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourMiseAJour(UtilisateurModele utilisateur, UUID uuid) {
        if (utilisateur == null) {
            throw new ValidationException("utilisateur.null");
        }

        // Validation de l'UUID (obligatoire pour une mise à jour)
        if (uuid == null) {
            throw new ValidationException("utilisateur.uuid.obligatoire");
        }
    }

    /**
     * Valide uniquement l'email
     *
     * @param email l'email à valider
     * @throws ValidationException si l'email est invalide
     */
    public void validerEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("utilisateur.email.obligatoire");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("utilisateur.email.format");
        }
    }

    /**
     * Valide uniquement le mot de passe
     *
     * @param motDePasse le mot de passe à valider
     * @throws ValidationException si le mot de passe est invalide
     */
    public void validerMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.isBlank()) {
            throw new ValidationException("utilisateur.motDePasse.obligatoire");
        }
        if (motDePasse.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("utilisateur.motDePasse.longueur", MIN_PASSWORD_LENGTH);
        }
    }

    /**
     * Valide uniquement le nom
     *
     * @param nom le nom à valider
     * @throws ValidationException si le nom est invalide
     */
    public void validerNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new ValidationException("utilisateur.nom.obligatoire");
        }
    }

    /**
     * Valide uniquement le prénom
     *
     * @param prenom le prénom à valider
     * @throws ValidationException si le prénom est invalide
     */
    public void validerPrenom(String prenom) {
        if (prenom == null || prenom.isBlank()) {
            throw new ValidationException("utilisateur.prenom.obligatoire");
        }
    }

    /**
     * Valide uniquement le rôle
     *
     * @param role le rôle à valider
     * @throws ValidationException si le rôle est invalide
     */
    public void validerRole(Object role) {
        if (role == null) {
            throw new ValidationException("utilisateur.role.obligatoire");
        }
    }
}
