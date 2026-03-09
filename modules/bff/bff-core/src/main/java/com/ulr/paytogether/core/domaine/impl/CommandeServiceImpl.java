package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.CommandeService;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.provider.CommandeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implémentation du service Commande
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommandeServiceImpl implements CommandeService {

    private final CommandeProvider commandeProvider;

    @Override
    public List<CommandeModele> lireToutesAvecInfosCompletes() {
        log.info("Récupération de toutes les commandes avec informations complètes");
        return commandeProvider.trouverToutesAvecInfosCompletes();
    }

    @Override
    public Map<String, Long> calculerStatistiques() {
        log.info("Calcul des statistiques des commandes");
        return commandeProvider.calculerStatistiquesCommandes();
    }
}

