package com.ulr.paytogether.core.domaine.service;

import com.ulr.paytogether.core.modele.CommandeModele;

import java.util.List;
import java.util.Map;

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
}

