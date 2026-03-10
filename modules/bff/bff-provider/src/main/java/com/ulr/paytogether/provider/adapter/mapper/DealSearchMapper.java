package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.DealRechercheModele;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.ImageDealJpa;
import com.ulr.paytogether.provider.adapter.entity.elasticsearch.DealDocument;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre DealJpa, DealModele et DealDocument (Elasticsearch)
 */
@Component
public class DealSearchMapper {

    /**
     * Convertit un DealJpa vers DealDocument
     * @param dealJpa Entité JPA
     * @return Document Elasticsearch
     */
    public DealDocument versDocument(DealJpa dealJpa) {
        if (dealJpa == null) {
            return null;
        }

        // Extraire l'image principale
        String imagePrincipaleUrl = dealJpa.getImageDealJpas() != null
                ? dealJpa.getImageDealJpas().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrincipal()))
                        .findFirst()
                        .map(ImageDealJpa::getUrlImage)
                        .orElse(null)
                : null;

        return DealDocument.builder()
                .id(dealJpa.getUuid().toString())
                .uuid(dealJpa.getUuid())
                .titre(dealJpa.getTitre())
                .description(dealJpa.getDescription())
                .prixDeal(dealJpa.getPrixDeal())
                .prixPart(dealJpa.getPrixPart())
                .nbParticipants(dealJpa.getNbParticipants())
                .dateDebut(dealJpa.getDateDebut())
                .dateFin(dealJpa.getDateFin())
                .statut(dealJpa.getStatut())
                .ville(dealJpa.getVille())
                .pays(dealJpa.getPays())
                .categorieUuid(dealJpa.getCategorieJpa() != null ? dealJpa.getCategorieJpa().getUuid() : null)
                .categorieNom(dealJpa.getCategorieJpa() != null ? dealJpa.getCategorieJpa().getNom() : null)
                .createurUuid(dealJpa.getMarchandJpa() != null ? dealJpa.getMarchandJpa().getUuid() : null)
                .createurNom(dealJpa.getMarchandJpa() != null
                        ? dealJpa.getMarchandJpa().getNom() + " " + dealJpa.getMarchandJpa().getPrenom()
                        : null)
                .imagePrincipaleUrl(imagePrincipaleUrl)
                .nombreDeVues(dealJpa.getNombreDeVues())
                .dateCreation(dealJpa.getDateCreation())
                .dateModification(dealJpa.getDateModification())
                .build();
    }

    /**
     * Convertit un DealModele vers DealDocument
     * @param dealModele Modèle métier
     * @return Document Elasticsearch
     */
    public DealDocument versDocument(DealModele dealModele) {
        if (dealModele == null) {
            return null;
        }

        // Extraire l'image principale
        String imagePrincipaleUrl = dealModele.getListeImages() != null
                ? dealModele.getListeImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrincipal()))
                        .findFirst()
                        .map(ImageDealModele::getUrlImage)
                        .orElse(null)
                : null;

        return DealDocument.builder()
                .id(dealModele.getUuid().toString())
                .uuid(dealModele.getUuid())
                .titre(dealModele.getTitre())
                .description(dealModele.getDescription())
                .prixDeal(dealModele.getPrixDeal())
                .prixPart(dealModele.getPrixPart())
                .nbParticipants(dealModele.getNbParticipants())
                .dateDebut(dealModele.getDateDebut())
                .dateFin(dealModele.getDateFin())
                .statut(dealModele.getStatut())
                .ville(dealModele.getVille())
                .pays(dealModele.getPays())
                .categorieUuid(dealModele.getCategorie() != null ? dealModele.getCategorie().getUuid() : null)
                .categorieNom(dealModele.getCategorie() != null ? dealModele.getCategorie().getNom() : null)
                .createurUuid(dealModele.getCreateur() != null ? dealModele.getCreateur().getUuid() : null)
                .createurNom(dealModele.getCreateur() != null
                        ? dealModele.getCreateur().getNom() + " " + dealModele.getCreateur().getPrenom()
                        : null)
                .imagePrincipaleUrl(imagePrincipaleUrl)
                .nombreDeVues(0) // DealModele n'a pas de nombreDeVues, on met 0 par défaut
                .dateCreation(dealModele.getDateCreation())
                .dateModification(dealModele.getDateModification())
                .build();
    }

    /**
     * Convertit un DealDocument vers DealRechercheModele
     * @param document Document Elasticsearch
     * @return Modèle de recherche
     */
    public DealRechercheModele versModeleRecherche(DealDocument document) {
        if (document == null) {
            return null;
        }

        return DealRechercheModele.builder()
                .uuid(document.getUuid())
                .titre(document.getTitre())
                .description(document.getDescription())
                .prixDeal(document.getPrixDeal())
                .prixPart(document.getPrixPart())
                .nbParticipants(document.getNbParticipants())
                .dateDebut(document.getDateDebut())
                .dateFin(document.getDateFin())
                .statut(document.getStatut())
                .ville(document.getVille())
                .pays(document.getPays())
                .categorieUuid(document.getCategorieUuid())
                .categorieNom(document.getCategorieNom())
                .createurUuid(document.getCreateurUuid())
                .createurNom(document.getCreateurNom())
                .imagePrincipaleUrl(document.getImagePrincipaleUrl())
                .nombreDeVues(document.getNombreDeVues())
                .dateCreation(document.getDateCreation())
                .build();
    }
}

