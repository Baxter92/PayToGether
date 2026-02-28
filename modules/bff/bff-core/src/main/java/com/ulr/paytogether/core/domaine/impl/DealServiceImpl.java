package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.domaine.validator.DealValidator;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.core.provider.DealProvider;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealProvider dealProvider;
    private final DealValidator dealValidator;

    @Override
    public DealModele creer(DealModele deal) {
        // Validation du deal avant création
        dealValidator.valider(deal);

        return dealProvider.sauvegarder(deal);
    }

    @Override
    public Optional<DealModele> lireParUuid(UUID uuid) {
        return dealProvider.trouverParUuid(uuid);
    }

    @Override
    public List<DealModele> lireTous() {
        return dealProvider.trouverTous();
    }

    @Override
    public List<DealModele> lireParStatut(StatutDeal statut) {
        return dealProvider.trouverParStatut(statut);
    }

    @Override
    public List<DealModele> lireParCreateur(UUID createurUuid) {
        return dealProvider.trouverParCreateur(createurUuid);
    }

    @Override
    public List<DealModele> lireParCategorie(UUID categorieUuid) {
        return dealProvider.trouverParCategorie(categorieUuid);
    }

    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        // Validation partielle du deal avant mise à jour (sans statut)
        dealValidator.validerPourMiseAJourPartielle(deal);

        // Séparer les images du deal pour traitement distinct
        List<ImageDealModele> listeImages = deal.getListeImages();
        boolean aDesImagesToUpdate = listeImages != null && !listeImages.isEmpty();

        // 1. Mise à jour des informations générales du deal
        DealModele dealMisAJour = dealProvider.mettreAJour(uuid, deal);

        // 2. Si des images sont présentes : traitement intelligent
        if (aDesImagesToUpdate) {
            // Valider les images
            dealValidator.validerImages(deal);

            // Mettre à jour les images (ajout/modification/suppression)
            DealModele dealAvecNouvellesImages = dealProvider.mettreAJourImages(uuid, deal);

            // Retourner le deal avec UNIQUEMENT les nouvelles images (avec presignUrl)
            dealMisAJour.setListeImages(dealAvecNouvellesImages.getListeImages());
        }else {
            // Si aucune image à mettre à jour, s'assurer que la liste d'images du deal retourné est vide
            dealMisAJour.setListeImages(List.of());
        }

        return dealMisAJour;
    }

    @Override
    public DealModele mettreAJourStatut(UUID uuid, StatutDeal nouveauStatut) {
        // Récupérer le deal existant
        DealModele dealExistant = dealProvider.trouverParUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "deal.non.trouve", uuid.toString()));

        // Valider la transition de statut
        dealValidator.validerTransitionStatut(dealExistant.getStatut(), nouveauStatut);

        // Mettre à jour le statut
        return dealProvider.mettreAJourStatut(uuid, nouveauStatut);
    }

    @Override
    public DealModele mettreAJourImages(UUID uuid, DealModele deal) {
        // Vérifier que le deal existe
        dealProvider.trouverParUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "deal.non.trouve", uuid.toString()));

        // Valider les images
        dealValidator.validerImages(deal);

        // Mettre à jour uniquement les images
        return dealProvider.mettreAJourImages(uuid, deal);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        dealProvider.supprimerParUuid(uuid);
    }

    @Override
    public Set<String> lireVillesDisponibles() {
        return dealProvider.trouverTous().stream()
                .filter(ville -> ville != null && StringUtils.isNotBlank(ville.getVille()) && ville.getVille().length() > 1)
                .map(d-> d.getVille().substring(0,1).toUpperCase() + d.getVille().substring(1).toLowerCase())
                .collect(Collectors.toSet());
    }

    @Override
    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut) {
        dealProvider.mettreAJourStatutImage(dealUuid, imageUuid, statut);
    }

    @Override
    public String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid) {
        return dealProvider.obtenirUrlLectureImage(dealUuid, imageUuid);
    }
}

