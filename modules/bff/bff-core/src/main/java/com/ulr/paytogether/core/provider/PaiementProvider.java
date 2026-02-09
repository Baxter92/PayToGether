package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.PaiementModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les paiements
 */
public interface PaiementProvider {

    PaiementModele sauvegarder(PaiementModele paiement);

    Optional<PaiementModele> trouverParUuid(UUID uuid);

    Optional<PaiementModele> trouverParTransactionId(String transactionId);

    List<PaiementModele> trouverParUtilisateur(UUID utilisateurUuid);

    List<PaiementModele> trouverParCommande(UUID commandeUuid);

    List<PaiementModele> trouverTous();

    void supprimerParUuid(UUID uuid);
}
