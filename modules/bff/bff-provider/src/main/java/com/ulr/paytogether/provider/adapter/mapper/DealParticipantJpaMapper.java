package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.AdresseModele;
import com.ulr.paytogether.core.modele.DealParticipantModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.provider.adapter.entity.AdresseJpa;
import com.ulr.paytogether.provider.adapter.entity.DealParticipantJpa;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper pour convertir entre DealParticipantJpa et DealParticipantModele
 */
@Component
@RequiredArgsConstructor
public class DealParticipantJpaMapper {

    /**
     * Convertit une entité JPA en modèle métier
     * @param jpa Entité JPA
     * @return Modèle métier
     */
    public DealParticipantModele versModele(DealParticipantJpa jpa) {
        if (jpa == null) {
            return null;
        }

        return DealParticipantModele.builder()
                .dealUuid(jpa.getId() != null ? jpa.getId().getDealUuid() : null)
                .utilisateurUuid(jpa.getId() != null ? jpa.getId().getUtilisateurUuid() : null)
                .nombreDePart(jpa.getNombreDePart())
                .dateParticipation(jpa.getDateParticipation())
                .dateModification(jpa.getDateModification())
                .build();
    }

    /**
     * Convertit un modèle métier en entité JPA
     * @param modele Modèle métier
     * @return Entité JPA
     */
    public DealParticipantJpa versEntite(DealParticipantModele modele) {
        if (modele == null) {
            return null;
        }

        DealParticipantJpa.DealParticipantId id = new DealParticipantJpa.DealParticipantId(
                modele.getDealUuid(),
                modele.getUtilisateurUuid()
        );

        return DealParticipantJpa.builder()
                .id(id)
                .nombreDePart(modele.getNombreDePart() != null ? modele.getNombreDePart() : 1)
                .dateParticipation(modele.getDateParticipation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Met à jour une entité JPA existante avec les données du modèle
     * @param jpa Entité JPA à mettre à jour
     * @param modele Modèle métier source
     */
    public void mettreAJour(DealParticipantJpa jpa, DealParticipantModele modele) {
        if (jpa == null || modele == null) {
            return;
        }

        if (modele.getNombreDePart() != null) {
            jpa.setNombreDePart(modele.getNombreDePart());
        }
    }

    /**
     * Convertit une entité JPA en modèle métier avec les informations utilisateur complètes
     * @param jpa Entité JPA
     * @return Modèle métier enrichi
     */
    public DealParticipantModele versModeleAvecUtilisateur(DealParticipantJpa jpa) {
        if (jpa == null) {
            return null;
        }

        DealParticipantModele modele = versModele(jpa);

        // Enrichir avec les informations utilisateur
        if (jpa.getUtilisateurJpa() != null) {
            UtilisateurModele utilisateur = UtilisateurModele.builder()
                    .uuid(jpa.getUtilisateurJpa().getUuid())
                    .nom(jpa.getUtilisateurJpa().getNom())
                    .prenom(jpa.getUtilisateurJpa().getPrenom())
                    .email(jpa.getUtilisateurJpa().getEmail())
                    .build();
            modele.setUtilisateur(utilisateur);
        }

        return modele;
    }

    /**
     * Convertit une entité JPA en modèle métier avec les informations utilisateur ET paiement
     * @param jpa Entité JPA
     * @param paiementJpa Paiement associé (peut être null)
     * @param adresseJpa Adresse associée au paiement (peut être null)
     * @param prixPart Prix d'une part du deal (pour calculer le montant total)
     * @return Modèle métier enrichi
     */
    public DealParticipantModele versModeleComplet(DealParticipantJpa jpa, PaiementJpa paiementJpa, AdresseJpa adresseJpa, BigDecimal prixPart) {
        if (jpa == null) {
            return null;
        }

        DealParticipantModele modele = versModeleAvecUtilisateur(jpa);

        // Enrichir avec les informations de paiement
        if (paiementJpa != null) {
            modele.setStatutPaiement(paiementJpa.getStatut());
            modele.setMontantTotal(paiementJpa.getMontant());

            // Enrichir avec l'adresse
            if (adresseJpa != null) {
                AdresseModele adresse = AdresseModele.builder()
                        .uuid(adresseJpa.getUuid())
                        .rue(adresseJpa.getRue())
                        .ville(adresseJpa.getVille())
                        .province(adresseJpa.getProvince())
                        .codePostal(adresseJpa.getCodePostal())
                        .pays(adresseJpa.getPays())
                        .appartement(adresseJpa.getAppartement())
                        .numeroPhone(adresseJpa.getNumeroPhone())
                        .build();
                modele.setAdresse(adresse);
            }
        } else if (prixPart != null && modele.getNombreDePart() != null) {
            // Si pas de paiement, calculer le montant théorique
            modele.setMontantTotal(prixPart.multiply(BigDecimal.valueOf(modele.getNombreDePart())));
        }

        return modele;
    }
}

