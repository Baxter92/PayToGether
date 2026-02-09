package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.PaiementDTO;
import com.ulr.paytogether.core.modele.PaiementModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre PaiementModele (Core) et PaiementDTO (API)
 */
@Component
public class PaiementMapper {

    /**
     * Convertit un modèle Core en DTO API
     */
    public PaiementDTO modeleVersDto(PaiementModele modele) {
        if (modele == null) {
            return null;
        }

        return PaiementDTO.builder()
                .uuid(modele.getUuid())
                .montant(modele.getMontant())
                .statut(modele.getStatut())
                .methodePaiement(modele.getMethodePaiement())
                .transactionId(modele.getTransactionId())
                .utilisateurUuid(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getUuid()
                        : null)
                .utilisateurNom(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getNom()
                        : null)
                .utilisateurPrenom(modele.getUtilisateur() != null
                        ? modele.getUtilisateur().getPrenom()
                        : null)
                .commandeUuid(modele.getCommande() != null
                        ? modele.getCommande().getUuid()
                        : null)
                .datePaiement(modele.getDatePaiement())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Convertit un DTO API en modèle Core
     */
    public PaiementModele dtoVersModele(PaiementDTO dto) {
        if (dto == null) {
            return null;
        }

        return PaiementModele.builder()
                .uuid(dto.getUuid())
                .montant(dto.getMontant())
                .statut(dto.getStatut())
                .methodePaiement(dto.getMethodePaiement())
                .transactionId(dto.getTransactionId())
                .datePaiement(dto.getDatePaiement())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
        // Note: L'utilisateur et la commande seront définis séparément par le service
    }
}
