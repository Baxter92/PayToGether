package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.api.dto.DealResponseDto;
import com.ulr.paytogether.api.dto.MiseAJourDealDTO;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Deal et DealDTO
 */
@Component
public class DealMapper {

    private final ImageDealMapper imageDealMapper;

    public DealMapper(ImageDealMapper imageDealMapper) {
        this.imageDealMapper = imageDealMapper;
    }
    /**
     * Convertit une entité Deal en DTO
     */
    public DealResponseDto versDTO(DealModele deal) {
        if (deal == null) {
            return null;
        }

        return DealResponseDto.builder()
                .uuid(deal.getUuid())
                .titre(deal.getTitre())
                .description(deal.getDescription())
                .prixDeal(deal.getPrixDeal())
                .prixPart(deal.getPrixPart())
                .nbParticipants(deal.getNbParticipants())
                .dateDebut(deal.getDateDebut())
                .dateFin(deal.getDateFin())
                .statut(deal.getStatut())
                .createurUuid(deal.getCreateur() != null ? deal.getCreateur().getUuid() : null)
                .createurNom(deal.getCreateur() != null ? deal.getCreateur().getNom() + " " + deal.getCreateur().getPrenom() : null)
                .categorieUuid(deal.getCategorie() != null ? deal.getCategorie().getUuid() : null)
                .categorieNom(deal.getCategorie() != null ? deal.getCategorie().getNom() : null)
                .listeImages(deal.getListeImages() != null ?
                    deal.getListeImages().stream()
                        .map(imageDealMapper::modeleVersDto)
                        .toList() : null)
                .listePointsForts(deal.getListePointsForts())
                .dateExpiration(deal.getDateExpiration())
                .ville(deal.getVille())
                .pays(deal.getPays())
                .dateCreation(deal.getDateCreation())
                .dateModification(deal.getDateModification())
                .build();
    }

    /**
     * Convertit un DTO en entité Deal (sans les relations)
     */
    public DealModele versEntite(DealDTO dto) {
        if (dto == null) {
            return null;
        }

        return DealModele.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .prixDeal(dto.getPrixDeal())
                .prixPart(dto.getPrixPart())
                .nbParticipants(dto.getNbParticipants())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .statut(dto.getStatut())
                .listeImages(dto.getListeImages() != null ?
                    dto.getListeImages().stream()
                        .map(imageDealMapper::dtoVersModele)
                        .toList() : null)
                .createur(dto.getCreateurUuid() != null ? UtilisateurModele.builder()
                        .uuid(dto.getCreateurUuid())
                        .build() : null)
                .categorie(dto.getCategorieUuid() != null ? CategorieModele.builder()
                        .uuid(dto.getCategorieUuid())
                        .build() : null)
                .listePointsForts(dto.getListePointsForts())
                .dateExpiration(dto.getDateExpiration())
                .ville(dto.getVille())
                .pays(dto.getPays())
                .build();
    }

    /**
     * Convertit un MiseAJourDealDTO en DealModele (sans statut et sans images)
     */
    public DealModele versEntite(MiseAJourDealDTO dto) {
        if (dto == null) {
            return null;
        }

        return DealModele.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .prixDeal(dto.getPrixDeal())
                .prixPart(dto.getPrixPart())
                .nbParticipants(dto.getNbParticipants())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .createur(dto.getCreateurUuid() != null ? UtilisateurModele.builder()
                        .uuid(dto.getCreateurUuid())
                        .build() : null)
                .categorie(dto.getCategorieUuid() != null ? CategorieModele.builder()
                        .uuid(dto.getCategorieUuid())
                        .build() : null)
                .listeImages(dto.getListeImages() != null ?
                    dto.getListeImages().stream()
                        .map(imageDealMapper::dtoVersModele)
                        .toList() : null)
                .listePointsForts(dto.getListePointsForts())
                .dateExpiration(dto.getDateExpiration())
                .ville(dto.getVille())
                .pays(dto.getPays())
                .build();
    }

    /**
     * Convertit un ImageDealDto vers ImageDealModele
     * Exposé publiquement pour utilisation dans les adaptateurs
     */
    public com.ulr.paytogether.core.modele.ImageDealModele imageDtoVersModele(com.ulr.paytogether.api.dto.ImageDealDto dto) {
        return imageDealMapper.dtoVersModele(dto);
    }

    /**
     * Met à jour une entité existante avec les données du DTO
     */
    public void mettreAJour(DealModele deal, DealDTO dto) {
        if (deal == null || dto == null) {
            return;
        }

        deal.setTitre(dto.getTitre());
        deal.setDescription(dto.getDescription());
        deal.setPrixDeal(dto.getPrixDeal());
        deal.setPrixPart(dto.getPrixPart());
        deal.setNbParticipants(dto.getNbParticipants());
        deal.setDateDebut(dto.getDateDebut());
        deal.setDateFin(dto.getDateFin());
        deal.setStatut(dto.getStatut());
        deal.setListeImages(dto.getListeImages() != null ?
            dto.getListeImages().stream()
                .map(imageDealMapper::dtoVersModele)
                .toList() : null);
        deal.setCreateur(dto.getCreateurUuid() != null ? UtilisateurModele.builder()
                .uuid(dto.getCreateurUuid())
                .build() : null);
        deal.setCategorie(dto.getCategorieUuid() != null ? CategorieModele.builder()
                .uuid(dto.getCategorieUuid())
                .build() : null);
        deal.setListePointsForts(dto.getListePointsForts());
        deal.setDateExpiration(dto.getDateExpiration());
        deal.setVille(dto.getVille());
        deal.setPays(dto.getPays());
    }
}
