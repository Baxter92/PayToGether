package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.CategorieModele;
import org.springframework.stereotype.Component;

/**
 * Validator pour les entités Catégorie
 * Centralise toutes les règles métier de validation
 */
@Component
public class CategorieValidator {

    // Longueur maximale du nom
    private static final int MAX_NOM_LENGTH = 100;

    /**
     * Validation complète d'une catégorie
     *
     * @param categorie le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void valider(CategorieModele categorie) {
        if (categorie == null) {
            throw new ValidationException("categorie.null");
        }

        // Validation du nom (obligatoire et non vide)
        if (categorie.getNom() == null || categorie.getNom().isBlank()) {
            throw new ValidationException("categorie.nom.obligatoire");
        }

        // Validation de la longueur du nom
        if (categorie.getNom().length() > MAX_NOM_LENGTH) {
            throw new ValidationException("categorie.nom.longueur", MAX_NOM_LENGTH);
        }
    }

    /**
     * Validation pour la mise à jour d'une catégorie
     *
     * @param categorie le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourMiseAJour(CategorieModele categorie) {
        if (categorie == null) {
            throw new ValidationException("categorie.null");
        }

        // Validation de l'UUID (obligatoire pour une mise à jour)
        if (categorie.getUuid() == null) {
            throw new ValidationException("categorie.uuid.obligatoire");
        }

        // Appel des validations générales
        valider(categorie);
    }

    /**
     * Valide uniquement le nom
     *
     * @param nom le nom à valider
     * @throws ValidationException si le nom est invalide
     */
    public void validerNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new ValidationException("categorie.nom.obligatoire");
        }
        if (nom.length() > MAX_NOM_LENGTH) {
            throw new ValidationException("categorie.nom.longueur", MAX_NOM_LENGTH);
        }
    }
}

