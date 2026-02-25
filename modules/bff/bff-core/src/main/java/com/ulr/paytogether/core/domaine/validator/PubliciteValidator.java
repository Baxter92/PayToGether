package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.PubliciteModele;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validator pour les entités Publicité
 * Centralise toutes les règles métier de validation
 */
@Component
public class PubliciteValidator {

    // Longueur maximale du titre
    private static final int MAX_TITRE_LENGTH = 200;

    // Longueur maximale de la description
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    /**
     * Validation complète d'une publicité
     *
     * @param publicite le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void valider(PubliciteModele publicite) {
        if (publicite == null) {
            throw new ValidationException("publicite.null");
        }

        // Validation du titre (obligatoire et non vide)
        if (publicite.getTitre() == null || publicite.getTitre().isBlank()) {
            throw new ValidationException("publicite.titre.obligatoire");
        }

        // Validation de la longueur du titre
        if (publicite.getTitre().length() > MAX_TITRE_LENGTH) {
            throw new ValidationException("publicite.titre.longueur", MAX_TITRE_LENGTH);
        }

        // Validation de la description (si présente)
        if (publicite.getDescription() != null && publicite.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("publicite.description.longueur", MAX_DESCRIPTION_LENGTH);
        }

        // Validation du lien externe (obligatoire et non vide)
        if (publicite.getLienExterne() == null || publicite.getLienExterne().isBlank()) {
            throw new ValidationException("publicite.lienExterne.obligatoire");
        }

        // Validation de la date de début (obligatoire)
        if (publicite.getDateDebut() == null) {
            throw new ValidationException("publicite.dateDebut.obligatoire");
        }

        // Validation de la date de fin (obligatoire)
        if (publicite.getDateFin() == null) {
            throw new ValidationException("publicite.dateFin.obligatoire");
        }

        // Validation de la cohérence des dates (date début < date fin)
        if (publicite.getDateDebut().isAfter(publicite.getDateFin())) {
            throw new ValidationException("publicite.dateFin.coherence");
        }

        // Validation de la liste des images (obligatoire et non vide)
        if (publicite.getListeImages() == null || publicite.getListeImages().isEmpty()) {
            throw new ValidationException("publicite.listeImages.obligatoire");
        }
    }

    /**
     * Validation pour la mise à jour d'une publicité
     *
     * @param publicite le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourMiseAJour(PubliciteModele publicite) {
        if (publicite == null) {
            throw new ValidationException("publicite.null");
        }

        // Validation de l'UUID (obligatoire pour une mise à jour)
        if (publicite.getUuid() == null) {
            throw new ValidationException("publicite.uuid.obligatoire");
        }

        // Appel des validations générales
        valider(publicite);
    }

    /**
     * Validation de cohérence métier pour l'activation d'une publicité
     *
     * @param publicite la publicité à activer
     * @throws ValidationException si la publicité ne peut pas être activée
     */
    public void validerActivation(PubliciteModele publicite) {
        if (publicite == null) {
            throw new ValidationException("publicite.null");
        }

        // Vérifier que la publicité n'est pas déjà active
        if (Boolean.TRUE.equals(publicite.getActive())) {
            throw new ValidationException("publicite.deja.active");
        }

        // Vérifier que la publicité n'est pas expirée
        if (publicite.getDateFin().isBefore(java.time.LocalDateTime.now())) {
            throw new ValidationException("publicite.expiree.impossible.activation");
        }
    }

    /**
     * Validation de cohérence métier pour la désactivation d'une publicité
     *
     * @param publicite la publicité à désactiver
     * @throws ValidationException si la publicité ne peut pas être désactivée
     */
    public void validerDesactivation(PubliciteModele publicite) {
        if (publicite == null) {
            throw new ValidationException("publicite.null");
        }

        // Vérifier que la publicité n'est pas déjà inactive
        if (Boolean.FALSE.equals(publicite.getActive())) {
            throw new ValidationException("publicite.deja.inactive");
        }
    }
}

