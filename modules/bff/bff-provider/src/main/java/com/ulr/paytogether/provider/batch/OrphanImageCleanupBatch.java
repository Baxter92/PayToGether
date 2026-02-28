package com.ulr.paytogether.provider.batch;

import com.ulr.paytogether.provider.repository.ImageDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * Batch de nettoyage des images orphelines
 * Supprime les images qui ne sont plus associées à aucun deal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanImageCleanupBatch {

    private final ImageDealRepository imageDealRepository;

    /**
     * Nettoie les images orphelines toutes les 24 heures
     * Cron: tous les jours à 2h00 du matin
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOrphanImages() {
        log.info("🧹 Démarrage du nettoyage des images orphelines...");

    }

}

