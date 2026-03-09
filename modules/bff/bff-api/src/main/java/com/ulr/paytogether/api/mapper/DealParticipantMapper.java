package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.DealParticipantDTO;
import com.ulr.paytogether.core.modele.DealParticipantModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre DealParticipantModele et DealParticipantDTO
 */
@Component
public class DealParticipantMapper {

    /**
     * Convertit un modèle en DTO
     */
    public DealParticipantDTO modeleVersDto(DealParticipantModele modele) {
        if (modele == null) {
            return null;
        }

        return DealParticipantDTO.builder()
                .dealUuid(modele.getDealUuid())
                .utilisateurUuid(modele.getUtilisateurUuid())
                .utilisateurNom(modele.getUtilisateur() != null ? modele.getUtilisateur().getNom() : null)
                .utilisateurPrenom(modele.getUtilisateur() != null ? modele.getUtilisateur().getPrenom() : null)
                .utilisateurEmail(modele.getUtilisateur() != null ? modele.getUtilisateur().getEmail() : null)
                .nombreDePart(modele.getNombreDePart())
                .dateParticipation(modele.getDateParticipation())
                .dateModification(modele.getDateModification())
                // Informations de paiement
                .montantTotal(modele.getMontantTotal())
                .statutPaiement(modele.getStatutPaiement())
                // Informations d'adresse
                .adresseRue(modele.getAdresse() != null ? modele.getAdresse().getRue() : null)
                .adresseVille(modele.getAdresse() != null ? modele.getAdresse().getVille() : null)
                .adresseProvince(modele.getAdresse() != null ? modele.getAdresse().getProvince() : null)
                .adresseCodePostal(modele.getAdresse() != null ? modele.getAdresse().getCodePostal() : null)
                .adressePays(modele.getAdresse() != null ? modele.getAdresse().getPays() : null)
                .adresseAppartement(modele.getAdresse() != null ? modele.getAdresse().getAppartement() : null)
                .adresseNumeroPhone(modele.getAdresse() != null ? modele.getAdresse().getNumeroPhone() : null)
                .build();
    }

    /**
     * Convertit un DTO en modèle
     */
    public DealParticipantModele dtoVersModele(DealParticipantDTO dto) {
        if (dto == null) {
            return null;
        }

        return DealParticipantModele.builder()
                .dealUuid(dto.getDealUuid())
                .utilisateurUuid(dto.getUtilisateurUuid())
                .nombreDePart(dto.getNombreDePart())
                .dateParticipation(dto.getDateParticipation())
                .dateModification(dto.getDateModification())
                .build();
    }
}

