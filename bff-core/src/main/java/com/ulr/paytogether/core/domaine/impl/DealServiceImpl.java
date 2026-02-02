package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutDeal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DealServiceImpl implements DealService {


    @Override
    public DealModele creer(DealModele deal) {
        return null;
    }

    @Override
    public Optional<DealModele> lireParUuid(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public List<DealModele> lireTous() {
        return List.of();
    }

    @Override
    public List<DealModele> lireParStatut(StatutDeal statut) {
        return List.of();
    }

    @Override
    public List<DealModele> lireParCreateur(UUID createurUuid) {
        return List.of();
    }

    @Override
    public List<DealModele> lireParCategorie(UUID categorieUuid) {
        return List.of();
    }

    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        return null;
    }

    @Override
    public void supprimerParUuid(UUID uuid) {

    }
}