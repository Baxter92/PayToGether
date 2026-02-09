package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.CommandeModele;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les commandes
 */
public interface CommandeProvider {

    CommandeModele sauvegarder(CommandeModele commande);

    Optional<CommandeModele> trouverParUuid(UUID uuid);

    List<CommandeModele> trouverParUtilisateur(UUID utilisateurUuid);

    List<CommandeModele> trouverParDeal(UUID dealUuid);

    List<CommandeModele> trouverTous();

    void supprimerParUuid(UUID uuid);
}
