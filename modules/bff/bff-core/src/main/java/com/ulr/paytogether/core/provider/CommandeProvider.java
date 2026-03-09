package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.modele.CommandeModele;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les commandes
 */
public interface CommandeProvider {

    CommandeModele sauvegarder(CommandeModele commande);

    Optional<CommandeModele> trouverParUuid(UUID uuid);

    List<CommandeModele> trouverParUtilisateur(UUID utilisateurUuid);

    CommandeModele trouverParDeal(UUID dealUuid);

    List<CommandeModele> trouverTous();

    void supprimerParUuid(UUID uuid);

    /**
     * Récupère toutes les commandes avec les informations complètes (marchand, deal, montant total paiements)
     * @return Liste des commandes enrichies
     */
    List<CommandeModele> trouverToutesAvecInfosCompletes();

    /**
     * Calcule les statistiques globales des commandes par statut
     * @return Map avec les statistiques (totalCommandes, confirmees, enCours, annulees, remboursees)
     */
    Map<String, Long> calculerStatistiquesCommandes();
}
