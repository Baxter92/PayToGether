package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.CommentaireModele;
import org.springframework.stereotype.Component;

/**
 * Validator pour les entités Commentaire
 * Centralise toutes les règles métier de validation
 */
@Component
public class CommentaireValidator {

    // Longueur maximale du contenu
    private static final int MAX_CONTENU_LENGTH = 2000;

    // Longueur minimale du contenu
    private static final int MIN_CONTENU_LENGTH = 1;

    /**
     * Validation complète d'un commentaire
     *
     * @param commentaire le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void valider(CommentaireModele commentaire) {
        if (commentaire == null) {
            throw new ValidationException("commentaire.null");
        }

        // Validation du contenu (obligatoire et non vide)
        if (commentaire.getContenu() == null || commentaire.getContenu().isBlank()) {
            throw new ValidationException("commentaire.contenu.obligatoire");
        }

        // Validation de la longueur minimale du contenu
        if (commentaire.getContenu().trim().length() < MIN_CONTENU_LENGTH) {
            throw new ValidationException("commentaire.contenu.longueur.min", MIN_CONTENU_LENGTH);
        }

        // Validation de la longueur maximale du contenu
        if (commentaire.getContenu().length() > MAX_CONTENU_LENGTH) {
            throw new ValidationException("commentaire.contenu.longueur.max", MAX_CONTENU_LENGTH);
        }

        // Validation de l'utilisateur (obligatoire)
        if (commentaire.getUtilisateur() == null || commentaire.getUtilisateur().getUuid() == null) {
            throw new ValidationException("commentaire.utilisateurUuid.obligatoire");
        }

        // Validation du deal (obligatoire)
        if (commentaire.getDeal() == null || commentaire.getDeal().getUuid() == null) {
            throw new ValidationException("commentaire.dealUuid.obligatoire");
        }
    }

    /**
     * Validation pour la mise à jour d'un commentaire
     *
     * @param commentaire le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourMiseAJour(CommentaireModele commentaire) {
        if (commentaire == null) {
            throw new ValidationException("commentaire.null");
        }

        // Validation de l'UUID (obligatoire pour une mise à jour)
        if (commentaire.getUuid() == null) {
            throw new ValidationException("commentaire.uuid.obligatoire");
        }

        // Appel des validations générales
        valider(commentaire);
    }

    /**
     * Valide uniquement le contenu
     *
     * @param contenu le contenu à valider
     * @throws ValidationException si le contenu est invalide
     */
    public void validerContenu(String contenu) {
        if (contenu == null || contenu.isBlank()) {
            throw new ValidationException("commentaire.contenu.obligatoire");
        }
        if (contenu.trim().length() < MIN_CONTENU_LENGTH) {
            throw new ValidationException("commentaire.contenu.longueur.min", MIN_CONTENU_LENGTH);
        }
        if (contenu.length() > MAX_CONTENU_LENGTH) {
            throw new ValidationException("commentaire.contenu.longueur.max", MAX_CONTENU_LENGTH);
        }
    }
}

