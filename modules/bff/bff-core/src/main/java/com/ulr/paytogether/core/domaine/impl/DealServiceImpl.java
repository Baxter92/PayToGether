package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.domaine.validator.DealValidator;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.enumeration.StatutDeal;
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
        // Validation du deal avant mise à jour
        dealValidator.valider(deal);

        return dealProvider.mettreAJour(uuid, deal);
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

