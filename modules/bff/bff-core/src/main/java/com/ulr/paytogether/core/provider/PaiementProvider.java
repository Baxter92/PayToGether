package com.ulr.paytogether.core.provider;

import com.ulr.paytogether.core.enumeration.StatutPaiement;
import com.ulr.paytogether.core.modele.PaiementModele;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) pour les opérations sur les paiements
 * Support Square Payment avec méthodes spécifiques
 */
public interface PaiementProvider {

    PaiementModele sauvegarder(PaiementModele paiement);

    PaiementModele mettreAJour(UUID uuid, PaiementModele paiement);

    Optional<PaiementModele> trouverParUuid(UUID uuid);

    Optional<PaiementModele> trouverParTransactionId(String transactionId);

    Optional<PaiementModele> trouverParSquarePaymentId(String squarePaymentId);

    List<PaiementModele> trouverParUtilisateur(UUID utilisateurUuid);

    List<PaiementModele> trouverParCommande(UUID commandeUuid);

    List<PaiementModele> trouverTous();

    void supprimerParUuid(UUID uuid);

    PaiementModele mettreAJourStatutCommandeDeal(UUID paiementUuid, String statut, int nombreDePart);

    /**
     * Récupère tous les paiements avec les informations complètes (client, marchand, deal, commande)
     * @return Liste des paiements enrichis
     */
    List<PaiementModele> trouverTousAvecInfosCompletes();

    /**
     * Calcule les statistiques globales des paiements
     * @return Map avec les statistiques (totalTransactions, transactionsReussies, transactionsEchouees, montantTotal)
     */
    Map<String, Object> calculerStatistiquesPaiements();

    /**
     * Récupère tous les paiements d'un utilisateur avec les informations complètes
     * (deal, catégorie, adresse de facturation, commande)
     * @param keycloakId String de l'utilisateur
     * @return Liste des paiements avec toutes les informations
     */
    List<PaiementModele> trouverParUtilisateurAvecInfosCompletes(String keycloakId);

    /**
     *
     * @param uuid
     */
    void supprimerParUtilisateur(UUID uuid);

    /**
     * Detacher le paiement de sa commande
     * @param uuid
     */
    void dettacherCommande(UUID uuid);
}
