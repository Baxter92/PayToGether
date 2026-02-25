package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.AdresseModele;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validator pour les entités Adresse
 * Centralise toutes les règles métier de validation
 */
@Component
public class AdresseValidator {

    // Pattern pour valider le code postal canadien (format: A1A 1A1)
    private static final Pattern CODE_POSTAL_PATTERN = Pattern.compile(
            "^[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d$"
    );

    // Longueur maximale de la rue
    private static final int MAX_RUE_LENGTH = 200;

    // Longueur maximale de la ville
    private static final int MAX_VILLE_LENGTH = 100;

    // Longueur maximale de la province
    private static final int MAX_PROVINCE_LENGTH = 50;

    /**
     * Validation complète d'une adresse
     *
     * @param adresse le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void valider(AdresseModele adresse) {
        if (adresse == null) {
            throw new ValidationException("adresse.null");
        }

        // Validation de la rue (obligatoire et non vide)
        if (adresse.getRue() == null || adresse.getRue().isBlank()) {
            throw new ValidationException("adresse.rue.obligatoire");
        }
        if (adresse.getRue().length() > MAX_RUE_LENGTH) {
            throw new ValidationException("adresse.rue.longueur", MAX_RUE_LENGTH);
        }

        // Validation de la ville (obligatoire et non vide)
        if (adresse.getVille() == null || adresse.getVille().isBlank()) {
            throw new ValidationException("adresse.ville.obligatoire");
        }
        if (adresse.getVille().length() > MAX_VILLE_LENGTH) {
            throw new ValidationException("adresse.ville.longueur", MAX_VILLE_LENGTH);
        }

        // Validation de la province (obligatoire et non vide)
        if (adresse.getProvince() == null || adresse.getProvince().isBlank()) {
            throw new ValidationException("adresse.province.obligatoire");
        }
        if (adresse.getProvince().length() > MAX_PROVINCE_LENGTH) {
            throw new ValidationException("adresse.province.longueur", MAX_PROVINCE_LENGTH);
        }

        // Validation du code postal (obligatoire et format valide)
        if (adresse.getCodePostal() == null || adresse.getCodePostal().isBlank()) {
            throw new ValidationException("adresse.codePostal.obligatoire");
        }
        if (!CODE_POSTAL_PATTERN.matcher(adresse.getCodePostal()).matches()) {
            throw new ValidationException("adresse.codePostal.format");
        }

        // Validation du pays (obligatoire et non vide)
        if (adresse.getPays() == null || adresse.getPays().isBlank()) {
            throw new ValidationException("adresse.pays.obligatoire");
        }

        // Validation de l'utilisateur (obligatoire)
        if (adresse.getUtilisateur() == null || adresse.getUtilisateur().getUuid() == null) {
            throw new ValidationException("adresse.utilisateurUuid.obligatoire");
        }
    }

    /**
     * Validation pour la mise à jour d'une adresse
     *
     * @param adresse le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourMiseAJour(AdresseModele adresse) {
        if (adresse == null) {
            throw new ValidationException("adresse.null");
        }

        // Validation de l'UUID (obligatoire pour une mise à jour)
        if (adresse.getUuid() == null) {
            throw new ValidationException("adresse.uuid.obligatoire");
        }

        // Appel des validations générales
        valider(adresse);
    }

    /**
     * Valide uniquement le code postal
     *
     * @param codePostal le code postal à valider
     * @throws ValidationException si le code postal est invalide
     */
    public void validerCodePostal(String codePostal) {
        if (codePostal == null || codePostal.isBlank()) {
            throw new ValidationException("adresse.codePostal.obligatoire");
        }
        if (!CODE_POSTAL_PATTERN.matcher(codePostal).matches()) {
            throw new ValidationException("adresse.codePostal.format");
        }
    }
}

