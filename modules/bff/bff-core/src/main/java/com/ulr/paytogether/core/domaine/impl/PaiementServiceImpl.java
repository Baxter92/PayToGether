package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.PaiementService;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.core.enumeration.StatutPaiement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaiementServiceImpl implements PaiementService {
    @Override
    public PaiementModele creer(PaiementModele deal) {
        return null;
    }

    @Override
    public Optional<PaiementModele> lireParUuid(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public List<PaiementModele> lireTous() {
        return List.of();
    }

    @Override
    public List<PaiementModele> lireParStatut(StatutPaiement statut) {
        return List.of();
    }

    @Override
    public List<PaiementModele> lireParDeal(UUID dealUuid) {
        return List.of();
    }

    @Override
    public DealModele mettreAJour(UUID uuid, PaiementModele paiement) {
        return null;
    }

    @Override
    public void supprimerParUuid(UUID uuid) {

    }
}