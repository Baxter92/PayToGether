package com.ulr.paytogether.core.domaine.validator;


import com.ulr.paytogether.core.modele.DealModele;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class DealValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return DealModele.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DealModele dealModele = (DealModele) target;

        // Validation du titre (obligatoire et non vide)
        if (dealModele.getTitre() == null || dealModele.getTitre().isBlank()) {
            errors.rejectValue("titre", "titre.obligatoire", "L'attribut titre est obligatoire");
        }

        // Validation du prix du deal (obligatoire et positif)
        if (dealModele.getPrixDeal() == null) {
            errors.rejectValue("prixDeal", "prixDeal.obligatoire", "L'attribut prixDeal est obligatoire");
        } else if (dealModele.getPrixDeal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.rejectValue("prixDeal", "prixDeal.positif", "Le prix du deal doit être positif");
        }

        // Validation du prix par part (obligatoire et positif)
        if (dealModele.getPrixPart() == null) {
            errors.rejectValue("prixPart", "prixPart.obligatoire", "L'attribut prixPart est obligatoire");
        } else if (dealModele.getPrixPart().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.rejectValue("prixPart", "prixPart.positif", "Le prix par part doit être positif");
        }

        // Validation du nombre de participants (obligatoire et positif)
        if (dealModele.getNbParticipants() == null) {
            errors.rejectValue("nbParticipants", "nbParticipants.obligatoire", "L'attribut nbParticipants est obligatoire");
        } else if (dealModele.getNbParticipants() <= 0) {
            errors.rejectValue("nbParticipants", "nbParticipants.positif", "Le nombre de participants doit être positif");
        }

        // Validation de la date de fin (obligatoire)
        if (dealModele.getDateFin() == null) {
            errors.rejectValue("dateFin", "dateFin.obligatoire", "L'attribut dateFin est obligatoire");
        }

        // Validation de la cohérence des dates (date début < date fin)
        if (dealModele.getDateDebut() != null && dealModele.getDateFin() != null) {
            if (dealModele.getDateDebut().isAfter(dealModele.getDateFin())) {
                errors.rejectValue("dateFin", "dateFin.coherence", "La date de fin doit être après la date de début");
            }
        }

        // Validation du statut (obligatoire)
        if (dealModele.getStatut() == null) {
            errors.rejectValue("statut", "statut.obligatoire", "L'attribut statut est obligatoire");
        }

        // Validation du créateur (obligatoire)
        if (dealModele.getCreateur() == null || dealModele.getCreateur().getUuid() == null) {
            errors.rejectValue("createurUuid", "createurUuid.obligatoire", "L'attribut createurUuid est obligatoire");
        }

        // Validation de la catégorie (obligatoire)
        if (dealModele.getCategorie() == null || dealModele.getCategorie().getUuid() == null) {
            errors.rejectValue("categorieUuid", "categorieUuid.obligatoire", "L'attribut categorieUuid est obligatoire");
        }

        // Validation de la liste des images (obligatoire et non vide)
        if (dealModele.getListeImages() == null || dealModele.getListeImages().isEmpty()) {
            errors.rejectValue("listeImages", "listeImages.obligatoire", "L'attribut listeImages est obligatoire");
        }

        // Validation de la ville (obligatoire)
        if (dealModele.getVille() == null || dealModele.getVille().isBlank()) {
            errors.rejectValue("ville", "ville.obligatoire", "L'attribut ville est obligatoire");
        }

        // Validation optionnelle de la description (longueur maximale)
        if (dealModele.getDescription() != null && dealModele.getDescription().length() > 5000) {
            errors.rejectValue("description", "description.longueur", "La description ne peut pas dépasser 5000 caractères");
        }
    }

    /**
     * Méthode de validation simplifiée qui lance des IllegalArgumentException
     * Utilisée dans les services métier
     *
     * @param dealModele le modèle à valider
     * @throws IllegalArgumentException si une validation échoue
     */
    public void valider(DealModele dealModele) {
        if (dealModele == null) {
            throw new IllegalArgumentException("Le deal ne peut pas être null");
        }

        // Validation du titre (obligatoire et non vide)
        if (dealModele.getTitre() == null || dealModele.getTitre().isBlank()) {
            throw new IllegalArgumentException("L'attribut titre est obligatoire");
        }

        // Validation du prix du deal (obligatoire et positif)
        if (dealModele.getPrixDeal() == null) {
            throw new IllegalArgumentException("L'attribut prixDeal est obligatoire");
        }
        if (dealModele.getPrixDeal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix du deal doit être positif");
        }

        // Validation du prix par part (obligatoire et positif)
        if (dealModele.getPrixPart() == null) {
            throw new IllegalArgumentException("L'attribut prixPart est obligatoire");
        }
        if (dealModele.getPrixPart().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix par part doit être positif");
        }

        // Validation du nombre de participants (obligatoire et positif)
        if (dealModele.getNbParticipants() == null) {
            throw new IllegalArgumentException("L'attribut nbParticipants est obligatoire");
        }
        if (dealModele.getNbParticipants() <= 0) {
            throw new IllegalArgumentException("Le nombre de participants doit être positif");
        }

        // Validation de la date de fin (obligatoire)
        if (dealModele.getDateFin() == null) {
            throw new IllegalArgumentException("L'attribut dateFin est obligatoire");
        }

        // Validation de la cohérence des dates (date début < date fin)
        if (dealModele.getDateDebut() != null && dealModele.getDateFin() != null) {
            if (dealModele.getDateDebut().isAfter(dealModele.getDateFin())) {
                throw new IllegalArgumentException("La date de fin doit être après la date de début");
            }
        }

        // Validation du statut (obligatoire)
        if (dealModele.getStatut() == null) {
            throw new IllegalArgumentException("L'attribut statut est obligatoire");
        }

        // Validation du créateur (obligatoire)
        if (dealModele.getCreateur() == null || dealModele.getCreateur().getUuid() == null) {
            throw new IllegalArgumentException("L'attribut createurUuid est obligatoire");
        }

        // Validation de la catégorie (obligatoire)
        if (dealModele.getCategorie() == null || dealModele.getCategorie().getUuid() == null) {
            throw new IllegalArgumentException("L'attribut categorieUuid est obligatoire");
        }

        // Validation de la liste des images (obligatoire et non vide)
        if (dealModele.getListeImages() == null || dealModele.getListeImages().isEmpty()) {
            throw new IllegalArgumentException("L'attribut listeImages est obligatoire");
        }

        // Validation de la ville (obligatoire)
        if (dealModele.getVille() == null || dealModele.getVille().isBlank()) {
            throw new IllegalArgumentException("L'attribut ville est obligatoire");
        }

        // Validation optionnelle de la description (longueur maximale)
        if (dealModele.getDescription() != null && dealModele.getDescription().length() > 5000) {
            throw new IllegalArgumentException("La description ne peut pas dépasser 5000 caractères");
        }
    }
}
