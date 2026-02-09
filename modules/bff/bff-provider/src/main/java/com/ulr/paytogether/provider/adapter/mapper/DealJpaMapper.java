package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre JpaDeal (entité JPA) et DealModele (modèle métier)
 */
@Component
@RequiredArgsConstructor
public class DealJpaMapper {

    private final UtilisateurJpaMapper utilisateurJpaMapper;
    private final CategorieJpaMapper categorieJpaMapper;
    private final ImageDealJpaMapper imageMapper;

    public DealModele versModele(DealJpa jpaDeal) {
        if (jpaDeal == null) {
            return null;
        }

        return DealModele.builder()
                .uuid(jpaDeal.getUuid())
                .titre(jpaDeal.getTitre())
                .description(jpaDeal.getDescription())
                .prixDeal(jpaDeal.getPrixDeal())
                .prixPart(jpaDeal.getPrixPart())
                .nbParticipants(jpaDeal.getNbParticipants())
                .dateDebut(jpaDeal.getDateDebut())
                .dateFin(jpaDeal.getDateFin())
                .statut(jpaDeal.getStatut())
                .createur(jpaDeal.getMarchandJpa() != null ? utilisateurJpaMapper.versModele(jpaDeal.getMarchandJpa()) : null)
                .categorie(jpaDeal.getCategorieJpa() != null ? categorieJpaMapper.versModele(jpaDeal.getCategorieJpa()) : null)
                .listeImages(jpaDeal.getImageDealJpas() != null ? jpaDeal.getImageDealJpas().stream().map(imageMapper::versModele).toList() : null)
                .listePointsForts(jpaDeal.getListePointsForts())
                .dateExpiration(jpaDeal.getDateExpiration())
                .ville(jpaDeal.getVille())
                .pays(jpaDeal.getPays())
                .dateCreation(jpaDeal.getDateCreation())
                .dateModification(jpaDeal.getDateModification())
                .build();
    }

    public DealJpa versEntite(DealModele modele) {
        if (modele == null) {
            return null;
        }

        return DealJpa.builder()
                .uuid(modele.getUuid())
                .titre(modele.getTitre())
                .description(modele.getDescription())
                .prixDeal(modele.getPrixDeal())
                .prixPart(modele.getPrixPart())
                .nbParticipants(modele.getNbParticipants())
                .dateDebut(modele.getDateDebut())
                .dateFin(modele.getDateFin())
                .statut(modele.getStatut())
                .marchandJpa(modele.getCreateur() != null ? utilisateurJpaMapper.versEntite(modele.getCreateur()) : null)
                .categorieJpa(modele.getCategorie() != null ? categorieJpaMapper.versEntite(modele.getCategorie()) : null)
                .imageDealJpas(modele.getListeImages() != null ? modele.getListeImages().stream().map(imageMapper::versEntite).toList() : null)
                .listePointsForts(modele.getListePointsForts())
                .dateExpiration(modele.getDateExpiration())
                .ville(modele.getVille())
                .pays(modele.getPays())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    public void mettreAJour(DealJpa entite, DealModele modele) {
        if (entite == null || modele == null) {
            return;
        }

        entite.setTitre(modele.getTitre());
        entite.setDescription(modele.getDescription());
        entite.setPrixDeal(modele.getPrixDeal());
        entite.setPrixPart(modele.getPrixPart());
        entite.setNbParticipants(modele.getNbParticipants());
        entite.setDateDebut(modele.getDateDebut());
        entite.setDateFin(modele.getDateFin());
        entite.setStatut(modele.getStatut());
        entite.setMarchandJpa(modele.getCreateur() != null ? utilisateurJpaMapper.versEntite(modele.getCreateur()) : null);
        entite.setCategorieJpa(modele.getCategorie() != null ? categorieJpaMapper.versEntite(modele.getCategorie()) : null);
        entite.setImageDealJpas(modele.getListeImages() != null ? modele.getListeImages().stream().map(imageMapper::versEntite).toList() : null);
        entite.setListePointsForts(modele.getListePointsForts());
        entite.setDateExpiration(modele.getDateExpiration());
        entite.setVille(modele.getVille());
        entite.setPays(modele.getPays());
    }
}
