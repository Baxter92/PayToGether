package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.AdresseModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les adresses
 */
public interface AdresseProvider {

    AdresseModele sauvegarder(AdresseModele adresse);

    Optional<AdresseModele> trouverParUuid(UUID uuid);

    AdresseModele trouverParPaiement(UUID paiementUuid);

    List<AdresseModele> trouverTous();

    void supprimerParUuid(UUID uuid);
}
