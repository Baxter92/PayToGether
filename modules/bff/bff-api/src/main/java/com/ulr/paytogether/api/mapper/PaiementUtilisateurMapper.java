package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.PaiementUtilisateurDTO;
import com.ulr.paytogether.core.modele.*;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir PaiementModele vers PaiementUtilisateurDTO
 */
@Component
@RequiredArgsConstructor
public class PaiementUtilisateurMapper {

    private final FileManager fileManager;

    /**
     * Convertit un PaiementModele vers PaiementUtilisateurDTO
     * avec toutes les informations complètes (deal, catégorie, adresse, commande)
     */
    public PaiementUtilisateurDTO versDTO(PaiementModele paiement) {
        if (paiement == null) {
            return null;
        }

        DealModele deal = paiement.getDeal();
        CommandeModele commande = paiement.getCommande();
        AdresseModele adresse = paiement.getAdresse();

        // Générer l'URL présignée pour l'image principale du deal
        String imagePrincipaleUrlPresignee = null;
        if (deal != null && deal.getListeImages() != null) {
            imagePrincipaleUrlPresignee = deal.getListeImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrincipal()))
                    .findFirst()
                    .map(img -> fileManager.generatePresignedUrlForRead(
                            Tools.DIRECTORY_DEALS_IMAGES + img.getUrlImage()))
                    .orElse(null);
        }

        // Construire le DTO du deal
        PaiementUtilisateurDTO.DealInfoDTO dealInfo = deal != null
                ? PaiementUtilisateurDTO.DealInfoDTO.builder()
                        .uuid(deal.getUuid())
                        .titre(deal.getTitre())
                        .description(deal.getDescription())
                        .imagePrincipaleUrlPresignee(imagePrincipaleUrlPresignee)
                        .dateExpiration(deal.getDateFin())
                        .prixDeal(deal.getPrixDeal())
                        .prixPart(deal.getPrixPart())
                        .build()
                : null;

        // Construire le DTO de la catégorie
        PaiementUtilisateurDTO.CategorieInfoDTO categorieInfo = deal != null && deal.getCategorie() != null
                ? PaiementUtilisateurDTO.CategorieInfoDTO.builder()
                        .uuid(deal.getCategorie().getUuid())
                        .nom(deal.getCategorie().getNom())
                        .icone(deal.getCategorie().getIcone())
                        .build()
                : null;

        // Construire le DTO de l'adresse de facturation
        PaiementUtilisateurDTO.AdresseFacturationDTO adresseFacturation = adresse != null
                ? PaiementUtilisateurDTO.AdresseFacturationDTO.builder()
                        .uuid(adresse.getUuid())
                        .rue(adresse.getRue())
                        .ville(adresse.getVille())
                        .codePostal(adresse.getCodePostal())
                        .telephone(adresse.getNumeroPhone())
                        .pays(adresse.getPays())
                        .build()
                : null;

        // Calculer le montant total (prixPart * nbParts)
        // nombreDePart est un int primitif, il ne peut pas être null
        Integer nbPartsAchetees = paiement.getNombreDePart() > 0 ? paiement.getNombreDePart() : 1;

        return PaiementUtilisateurDTO.builder()
                // Informations du paiement
                .paiementUuid(paiement.getUuid())
                .montantPaiement(paiement.getMontant())
                .statutPaiement(paiement.getStatut() != null ? paiement.getStatut().name() : null)
                .methodePaiement(paiement.getMethodePaiement() != null ? paiement.getMethodePaiement().name() : null)
                .datePaiement(paiement.getDatePaiement())
                .transactionId(paiement.getTransactionId())
                // Informations de la commande
                .commandeUuid(commande != null ? commande.getUuid() : null)
                .numeroCommande(commande != null ? commande.getNumeroCommande() : null)
                .statutCommande(commande != null && commande.getStatut() != null ? commande.getStatut().name() : null)
                .montantTotal(commande != null ? commande.getMontantTotal() : paiement.getMontant())
                .nbPartsAchetees(nbPartsAchetees)
                // Informations du deal, catégorie et adresse
                .deal(dealInfo)
                .categorie(categorieInfo)
                .adresseFacturation(adresseFacturation)
                .build();
    }

    /**
     * Convertit une liste de PaiementModele vers une liste de PaiementUtilisateurDTO
     */
    public List<PaiementUtilisateurDTO> versDTOList(List<PaiementModele> paiements) {
        if (paiements == null) {
            return List.of();
        }

        return paiements.stream()
                .map(this::versDTO)
                .collect(Collectors.toList());
    }
}

