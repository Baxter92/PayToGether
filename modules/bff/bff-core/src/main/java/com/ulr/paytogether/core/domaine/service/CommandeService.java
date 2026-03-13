package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.CommandeUtilisateurModele;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface du service Commande
 */
public interface CommandeService {

    /**
     * Lire toutes les commandes avec informations complètes (pour l'admin)
     */
    List<CommandeModele> lireToutesAvecInfosCompletes();

    /**
     * Calculer les statistiques des commandes (pour l'admin)
     */
    Map<String, Long> calculerStatistiques();

    /**
     * Lire une commande par l'UUID d'un paiement associé
     * @param paiementUuid UUID du paiement
     * @return Commande associée au paiement, ou null si aucune commande trouvée
     */
    CommandeModele lireParPaiementUuid(UUID paiementUuid);

    /**
     * Lire une commande par son UUID
     * @param uuid UUID de la commande
     * @return Commande correspondante, ou null si aucune commande trouvée
     */
    CommandeModele lireParUuid(UUID uuid);
    
    /**
     * Valider un payout par l'admin
     * Change le statut de la commande en PAYOUT et envoie un mail au vendeur
     * @param commandeUuid UUID de la commande
     * @param dateDepotPayout Date de dépôt du payout
     * @return Commande mise à jour
     */
    CommandeModele validerPayout(UUID commandeUuid, LocalDateTime dateDepotPayout);
    
    /**
     * Upload de la facture du vendeur
     * Change le statut en INVOICE_SELLER et déclenche la génération des factures clients
     * @param commandeUuid UUID de la commande
     * @param factureData Contenu du fichier de la facture
     * @param factureNom Nom du fichier de la facture
     * @return Commande mise à jour
     */
    CommandeModele uploadFactureVendeur(UUID commandeUuid, byte[] factureData, String factureNom);
    
    /**
     * Valider les factures des clients par l'admin
     * @param commandeUuid UUID de la commande
     * @param utilisateurUuids Liste des UUIDs des utilisateurs validés
     * @return Informations sur les validations
     */
    Map<String, Object> validerFacturesClients(UUID commandeUuid, List<UUID> utilisateurUuids);
    
    /**
     * Mettre à jour le statut d'une commande
     * @param commandeUuid UUID de la commande
     * @param nouveauStatut Nouveau statut de la commande
     * @return Commande mise à jour
     */
    CommandeModele mettreAJourStatutCommande(UUID commandeUuid, StatutCommande nouveauStatut);

    /**
     *
     * @param commandeUuid
     * @return
     */
    List<CommandeUtilisateurModele> listerUtilisateursParCommande(UUID commandeUuid);
}
