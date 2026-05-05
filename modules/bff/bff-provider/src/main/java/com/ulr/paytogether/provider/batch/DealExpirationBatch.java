package com.ulr.paytogether.provider.batch;

import com.ulr.paytogether.core.domaine.service.DealService;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.event.DealCancelledEvent;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.core.modele.DealModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Batch de vérification et mise à jour des deals expirés.
 *
 * ⚡ PERF : Cette vérification était auparavant effectuée de manière synchrone
 * dans le chemin de lecture lireParStatut() — ce qui ajoutait une requête +
 * des UPDATE potentiels à chaque appel public GET /deals/statut/PUBLIE.
 *
 * Elle est maintenant déléguée à un @Scheduled exécuté toutes les 15 minutes,
 * ce qui élimine complètement cette latence sur les endpoints publics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DealExpirationBatch {

    private final DealService dealService;
    private final EventPublisher eventPublisher;

    /**
     * Vérifie et expire les deals dont la dateFin est dépassée.
     * Exécuté toutes les 15 minutes.
     */
    @Scheduled(cron = "${deal.expiration.cron:0 0 1 * * *}") // Par défaut, tous les jours à 1h du matin (cron standard)
    @Transactional
    public void verifierEtExpirerDeals() {
        log.debug("⏱️ [DealExpirationBatch] Vérification des deals expirés...");

        List<DealModele> dealsPublies = dealService.lireParStatut(StatutDeal.PUBLIE);
        LocalDateTime maintenant = LocalDateTime.now();
        int nombreDealsExpires = 0;

        for (DealModele deal : dealsPublies) {
            if (deal.getDateExpiration() != null && deal.getDateExpiration().isBefore(maintenant)) {
                log.info("Deal {} expiré (date expiration: {})", deal.getUuid(), deal.getDateExpiration());

                dealService.mettreAJourStatut(deal.getUuid(), StatutDeal.EXPIRE);
                nombreDealsExpires++;

                // Publier l'événement d'annulation pour notifier les participants
                var dealAnnuleEvent = DealCancelledEvent.builder()
                        .dealUuid(deal.getUuid())
                        .nomMarchand(deal.getCreateur().getNom())
                        .emailMarchand(deal.getCreateur().getEmail())
                        .prenomMarchand(deal.getCreateur().getPrenom())
                        .marchandUuid(deal.getCreateur().getUuid())
                        .titreDeal(deal.getTitre())
                        .dateAnnulation(maintenant)
                        .raisonAnnulation("Expiration automatique du deal")
                        .build();

                eventPublisher.publishAsync(dealAnnuleEvent);
            }
        }

        if (nombreDealsExpires > 0) {
            log.info("✅ [DealExpirationBatch] {} deal(s) expirés mis à jour", nombreDealsExpires);
        } else {
            log.debug("✅ [DealExpirationBatch] Aucun deal expiré");
        }
    }
}



