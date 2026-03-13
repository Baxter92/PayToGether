package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.CommandeUtilisateurModele;

import java.time.LocalDateTime;
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

    /**
     * Trouve une commande par l'UUID d'un paiement associé
     * @param paiementUuid UUID du paiement
     * @return Commande associée au paiement, ou null si aucune commande trouvée
     */
    CommandeModele trouverParPaiementUuid(UUID paiementUuid);

    /**
     * Trouve une commande par son UUID
     * @param uuid UUID de la commande
     * @return Commande correspondante, ou null si aucune commande trouvée
     */
    CommandeModele lireParUuid(UUID uuid);
    
    /**
     * Met à jour le statut d'une commande et sa date de dépôt payout
     */
    CommandeModele mettreAJourStatutEtDatePayout(UUID commandeUuid, 
                                                  StatutCommande statut,
                                                  LocalDateTime dateDepotPayout);
    
    /**
     * Met à jour l'URL de la facture du marchand
     */
    CommandeModele mettreAJourFactureMarchand(UUID commandeUuid, String factureUrl);
    
    /**
     * Récupère les utilisateurs d'une commande via la table commande_utilisateur
     */
    List<CommandeUtilisateurModele> trouverUtilisateursCommande(UUID commandeUuid);
    
    /**
     * Valide un utilisateur pour une commande
     */
    void validerUtilisateurCommande(UUID commandeUuid, UUID utilisateurUuid);
    
    /**
     * Vérifie si tous les utilisateurs d'une commande sont validés
     */
    boolean tousUtilisateursValides(UUID commandeUuid);
}
