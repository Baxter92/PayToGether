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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealProvider dealProvider;
    private final DealValidator dealValidator;

    @Transactional
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

    @Transactional
    @Override
    public List<DealModele> lireParStatut(StatutDeal statut) {
        // Vérifier et mettre à jour les deals expirés avant de retourner les résultats
        verifierEtMettreAJourDealsExpires();

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

    @Transactional
    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        // Validation partielle du deal avant mise à jour (sans statut)
        dealValidator.validerPourMiseAJourPartielle(deal);

        // Séparer les images du deal pour traitement distinct
        List<ImageDealModele> listeImages = deal.getListeImages();
        boolean aDesImagesToUpdate = listeImages != null && !listeImages.isEmpty();

        // Mettre à jour le deal sans les images
        DealModele dealMisAJour = dealProvider.mettreAJour(uuid, deal);

        // Si des images sont présentes : traiter les images séparément
        if (aDesImagesToUpdate) {
            // 1. Valider les images
            dealValidator.validerImages(deal);

            // 2. Collecter les UUIDs des images envoyées dans le DTO
            List<UUID> uuidsEnvoyes = listeImages.stream()
                    .map(ImageDealModele::getUuid)
                    .filter(Objects::nonNull)
                    .toList();

            // 3. Supprimer les images en BD dont l'UUID n'est PAS dans le DTO
            dealProvider.supprimerImagesNonPresentes(uuid, uuidsEnvoyes);

            // 4. Liste pour stocker uniquement les nouvelles images créées
            List<ImageDealModele> nouvellesImagesCreees = new ArrayList<>();

            // 5. Parcourir les images du DTO
            for (ImageDealModele imageModele : listeImages) {
                if (imageModele.getUuid() == null) {
                    // 5.1. AJOUTER nouvelle image (UUID null)
                    ImageDealModele nouvelleImage = dealProvider.ajouterImage(uuid, imageModele);
                    nouvellesImagesCreees.add(nouvelleImage);
                } else {
                    // 5.2. METTRE À JOUR image existante (si elle existe)
                    Optional<ImageDealModele> imageExistante = dealProvider.trouverImageParUuid(uuid, imageModele.getUuid());
                    if (imageExistante.isPresent()) {
                        dealProvider.mettreAJourImageExistante(uuid, imageModele.getUuid(), imageModele);
                    }
                }
            }

            // 6. Retourner le deal avec UNIQUEMENT les nouvelles images créées
            dealMisAJour.setListeImages(nouvellesImagesCreees);
        } else {
            // Pas d'images à traiter
            dealMisAJour.setListeImages(List.of());
        }

        return dealMisAJour;
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    @Override
    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut) {
        dealProvider.mettreAJourStatutImage(dealUuid, imageUuid, statut);
    }

    @Override
    public String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid) {
        return dealProvider.obtenirUrlLectureImage(dealUuid, imageUuid);
    }

    @Transactional
    @Override
    public void verifierEtMettreAJourDealsExpires() {
        log.debug("Vérification des deals expirés...");

        // Récupérer tous les deals PUBLIE
        List<DealModele> dealsPublies = dealProvider.trouverParStatut(StatutDeal.PUBLIE);

        LocalDateTime maintenant = LocalDateTime.now();
        int nombreDealsExpires = 0;

        for (DealModele deal : dealsPublies) {
            // Vérifier si la date d'expiration est dépassée
            if (deal.getDateExpiration() != null && deal.getDateExpiration().isBefore(maintenant)) {
                log.info("Deal {} expiré (date expiration: {})", deal.getUuid(), deal.getDateExpiration());
                dealProvider.mettreAJourStatut(deal.getUuid(), StatutDeal.EXPIRE);
                nombreDealsExpires++;
            }
        }

        if (nombreDealsExpires > 0) {
            log.info("{} deal(s) mis à jour vers le statut EXPIRE", nombreDealsExpires);
        }
    }
}
