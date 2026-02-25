package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.PaiementModele;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validator pour les entités Paiement
 * Centralise toutes les règles métier de validation
 */
@Component
public class PaiementValidator {

    /**
     * Validation complète d'un paiement
     *
     * @param paiement le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void valider(PaiementModele paiement) {
        if (paiement == null) {
            throw new ValidationException("paiement.null");
        }

        // Validation du montant (obligatoire et positif)
        if (paiement.getMontant() == null) {
            throw new ValidationException("paiement.montant.obligatoire");
        }
        if (paiement.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("paiement.montant.positif");
        }

        // Validation de la méthode de paiement (obligatoire)
        if (paiement.getMethode() == null) {
            throw new ValidationException("paiement.methode.obligatoire");
        }

        // Validation du statut (obligatoire)
        if (paiement.getStatut() == null) {
            throw new ValidationException("paiement.statut.obligatoire");
        }

        // Validation du type de paiement (obligatoire)
        if (paiement.getType() == null) {
            throw new ValidationException("paiement.type.obligatoire");
        }

        // Validation de l'utilisateur (obligatoire)
        if (paiement.getUtilisateur() == null || paiement.getUtilisateur().getUuid() == null) {
            throw new ValidationException("paiement.utilisateurUuid.obligatoire");
        }

        // Validation de la commande (obligatoire)
        if (paiement.getCommande() == null || paiement.getCommande().getUuid() == null) {
            throw new ValidationException("paiement.commandeUuid.obligatoire");
        }
    }

    /**
     * Validation pour la mise à jour d'un paiement
     *
     * @param paiement le modèle à valider
     * @throws ValidationException si une validation échoue
     */
    public void validerPourMiseAJour(PaiementModele paiement) {
        if (paiement == null) {
            throw new ValidationException("paiement.null");
        }

        // Validation de l'UUID (obligatoire pour une mise à jour)
        if (paiement.getUuid() == null) {
            throw new ValidationException("paiement.uuid.obligatoire");
        }

        // Appel des validations générales
        valider(paiement);
    }

    /**
     * Validation métier pour le remboursement d'un paiement
     *
     * @param paiement le paiement à rembourser
     * @throws ValidationException si le paiement ne peut pas être remboursé
     */
    public void validerRemboursement(PaiementModele paiement) {
        if (paiement == null) {
            throw new ValidationException("paiement.null");
        }

        // Vérifier que le paiement est réussi
        if (paiement.getStatut() != com.ulr.paytogether.core.enumeration.StatutPaiement.REUSSI) {
            throw new ValidationException("paiement.doit.etre.reussi");
        }

        // Vérifier que le paiement n'est pas déjà remboursé
        if (paiement.getStatut() == com.ulr.paytogether.core.enumeration.StatutPaiement.REMBOURSE) {
            throw new ValidationException("paiement.deja.rembourse");
        }
    }

    /**
     * Validation métier pour la confirmation d'un paiement
     *
     * @param paiement le paiement à confirmer
     * @throws ValidationException si le paiement ne peut pas être confirmé
     */
    public void validerConfirmation(PaiementModele paiement) {
        if (paiement == null) {
            throw new ValidationException("paiement.null");
        }

        // Vérifier que le paiement est en attente
        if (paiement.getStatut() != com.ulr.paytogether.core.enumeration.StatutPaiement.EN_ATTENTE) {
            throw new ValidationException("paiement.doit.etre.en.attente");
        }
    }

    /**
     * Validation du montant
     *
     * @param montant le montant à valider
     * @throws ValidationException si le montant est invalide
     */
    public void validerMontant(BigDecimal montant) {
        if (montant == null) {
            throw new ValidationException("paiement.montant.obligatoire");
        }
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("paiement.montant.positif");
        }
    }
}

