package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.CommandeModele;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validator pour les entités Commande
 * Centralise toutes les règles métier de validation
 */
@Component
public class CommandeValidator {

    /**
     * Validation complète d'une commande
     *
     * @param commande le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void valider(CommandeModele commande) {
        if (commande == null) {
            throw new ValidationException("commande.null");
        }

        // Validation du montant total (obligatoire et positif)
        if (commande.getMontantTotal() == null) {
            throw new ValidationException("commande.montantTotal.obligatoire");
        }
        if (commande.getMontantTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("commande.montantTotal.positif");
        }

        // Validation du statut (obligatoire)
        if (commande.getStatut() == null) {
            throw new ValidationException("commande.statut.obligatoire");
        }

        // Validation de l'utilisateur (obligatoire)
        if (commande.getUtilisateur() == null || commande.getUtilisateur().getUuid() == null) {
            throw new ValidationException("commande.utilisateurUuid.obligatoire");
        }

        // Validation du deal (obligatoire)
        if (commande.getDealModele() == null || commande.getDealModele().getUuid() == null) {
            throw new ValidationException("commande.dealUuid.obligatoire");
        }
    }

    /**
     * Validation pour la mise à jour d'une commande
     *
     * @param commande le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourMiseAJour(CommandeModele commande) {
        if (commande == null) {
            throw new ValidationException("commande.null");
        }

        // Validation de l'UUID (obligatoire pour une mise à jour)
        if (commande.getUuid() == null) {
            throw new ValidationException("commande.uuid.obligatoire");
        }

        // Appel des validations générales
        valider(commande);
    }

    /**
     * Validation métier pour l'annulation d'une commande
     *
     * @param commande la commande à annuler
     * @throws ValidationException si la commande ne peut pas être annulée
     */
    public void validerAnnulation(CommandeModele commande) {
        if (commande == null) {
            throw new ValidationException("commande.null");
        }

        // Vérifier que la commande n'est pas déjà annulée
        if (commande.getStatut() == com.ulr.paytogether.core.enumeration.StatutCommande.ANNULEE) {
            throw new ValidationException("commande.deja.annulee");
        }
    }

    /**
     * Validation métier pour la confirmation d'une commande
     *
     * @param commande la commande à confirmer
     * @throws ValidationException si la commande ne peut pas être confirmée
     */
    public void validerConfirmation(CommandeModele commande) {
        if (commande == null) {
            throw new ValidationException("commande.null");
        }

        // Vérifier que la commande est en cours
        if (commande.getStatut() != com.ulr.paytogether.core.enumeration.StatutCommande.EN_COURS) {
            throw new ValidationException("commande.doit.etre.en.cours");
        }
    }

    /**
     * Validation métier pour le paiement d'une commande
     *
     * @param commande la commande à payer
     * @throws ValidationException si la commande ne peut pas être payée
     */
    public void validerPaiement(CommandeModele commande) {
        if (commande == null) {
            throw new ValidationException("commande.null");
        }

        // Vérifier que la commande est confirmée
        if (commande.getStatut() != com.ulr.paytogether.core.enumeration.StatutCommande.CONFIRMEE) {
            throw new ValidationException("commande.doit.etre.confirmee");
        }
    }
}

