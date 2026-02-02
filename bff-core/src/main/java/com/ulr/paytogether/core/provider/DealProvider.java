package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.provider.adapter.entity.enumeration.StatutDeal;
import com.ulr.paytogether.core.modele.DealModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les deals
 */
public interface DealProvider {

    DealModele sauvegarder(DealModele deal);

    Optional<DealModele> trouverParUuid(UUID uuid);

    List<DealModele> trouverTous();

    List<DealModele> trouverParStatut(StatutDeal statut);

    List<DealModele> trouverParCreateur(UUID createurUuid);

    List<DealModele> trouverParCategorie(UUID categorieUuid);

    void supprimerParUuid(UUID uuid);
}
