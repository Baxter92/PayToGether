package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.CommandeModele;

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
}

