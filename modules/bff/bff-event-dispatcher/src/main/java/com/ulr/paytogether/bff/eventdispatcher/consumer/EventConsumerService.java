package com.ulr.paytogether.bff.eventdispatcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service responsable de la consommation des événements.
 *
 * ✅ ARCHITECTURE ANTI-DOUBLON / ANTI-OPTIMISTIC-LOCK :
 * - processePendingEvents() : PAS @Transactional → pas de session JPA partagée
 * - EventTransactionProcessor : chaque événement dans sa propre transaction REQUIRES_NEW
 * - Claim atomique BDD : UPDATE ... WHERE status='PENDING' → 0 row = déjà pris → skip
 *
 * Cette architecture évite :
 * - ObjectOptimisticLockingFailureException (conflits @Version inter-événements)
 * - Doublons de traitement (multi-thread / multi-pod Kubernetes)
 * - Rollback en cascade (un échec n'annule pas les autres événements du batch)
 */
@Service
@Slf4j
public class EventConsumerService {

    private final EventRecordRepository eventRecordRepository;
    private final ApplicationContext applicationContext;
    private final EventTransactionProcessor eventTransactionProcessor;
    private final ObjectMapper objectMapper;
    private final List<EventTransactionProcessor.HandlerRegistre> handlersRegistres = new ArrayList<>();

    @Autowired
    public EventConsumerService(EventRecordRepository eventRecordRepository,
                                ApplicationContext applicationContext,
                                EventTransactionProcessor eventTransactionProcessor) {
        this.eventRecordRepository = eventRecordRepository;
        this.applicationContext = applicationContext;
        this.eventTransactionProcessor = eventTransactionProcessor;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        decouvririrHandlers();
    }

    /**
     * Découvre tous les handlers annotés avec @FunctionalHandler au démarrage.
     */
    private void decouvririrHandlers() {
        log.info("🔍 Découverte des handlers d'événements...");

        String[] nomsBeans = applicationContext.getBeanNamesForType(ConsumerHandler.class);
        log.info("Trouvé {} beans implémentant ConsumerHandler", nomsBeans.length);

        for (String nomBean : nomsBeans) {
            Object bean = applicationContext.getBean(nomBean);
            Class<?> classeReelle = org.springframework.aop.support.AopUtils.getTargetClass(bean);

            log.info("Scan bean: {} → classe réelle: {}", nomBean, classeReelle.getName());

            for (Method methode : classeReelle.getDeclaredMethods()) {
                if (methode.isAnnotationPresent(FunctionalHandler.class)) {
                    FunctionalHandler annotation = methode.getAnnotation(FunctionalHandler.class);

                    EventTransactionProcessor.HandlerRegistre handler =
                            new EventTransactionProcessor.HandlerRegistre(
                                    bean,
                                    methode,
                                    annotation.eventType(),
                                    annotation.maxAttempts(),
                                    classeReelle.getSimpleName() + "." + methode.getName()
                            );

                    handlersRegistres.add(handler);
                    log.info("✅ Handler enregistré: {} → type événement: {}",
                            handler.getNomHandler(), handler.getTypeEvent().getSimpleName());
                }
            }
        }

        log.info("========================================");
        log.info("Total handlers enregistrés: {}", handlersRegistres.size());
        log.info("========================================");

        if (handlersRegistres.isEmpty()) {
            log.warn("⚠️ AUCUN HANDLER ENREGISTRÉ ! Vérifier le scan du module bff-event.");
        }
    }

    /**
     * Traite les événements PENDING toutes les 120 secondes.
     *
     * ✅ PAS @Transactional ici :
     * - Évite une session JPA partagée pour tous les événements du batch
     * - Chaque événement est traité dans sa propre transaction via EventTransactionProcessor
     * - Un échec sur un événement ne rollback pas les autres
     */
    @Scheduled(fixedDelay = 120000, initialDelay = 30000)
    public void processePendingEvents() {
        // Lecture simple hors transaction (pas de session JPA ouverte)
        List<EventRecordJpa> evenementsPending = eventRecordRepository.findByStatusOrderByOccurredOnAsc(
                EventRecordJpa.EventStatus.PENDING,
                PageRequest.of(0, 10)
        );

        if (evenementsPending.isEmpty()) {
            // Vérifier les bloqués même si pas de PENDING
            eventTransactionProcessor.reinitialiserEvenementsBloques();
            return;
        }

        log.info("⚙️ Traitement batch de {} événements PENDING (max 10 par batch)", evenementsPending.size());

        int succes = 0;
        int echecs = 0;
        int skips = 0;

        for (EventRecordJpa evenement : evenementsPending) {
            try {
                // Trouver les handlers compatibles (lecture mémoire, pas de BDD)
                List<EventTransactionProcessor.HandlerRegistre> handlersCompatibles =
                        trouverHandlersCompatibles(evenement);

                if (handlersCompatibles.isEmpty()) {
                    log.warn("⚠️ Aucun handler pour le type d'événement: {}", evenement.getEventType());
                    // Marquer comme FAILED dans sa propre transaction via le processor
                    eventTransactionProcessor.processerEvenement(
                            evenement.getEventId(), List.of(), objectMapper);
                    echecs++;
                    continue;
                }

                // ✅ Traitement dans REQUIRES_NEW → transaction isolée
                boolean traite = eventTransactionProcessor.processerEvenement(
                        evenement.getEventId(),
                        handlersCompatibles,
                        objectMapper
                );

                if (traite) {
                    succes++;
                } else {
                    skips++;
                }

            } catch (Exception e) {
                echecs++;
                log.error("❌ Erreur lors du traitement de l'événement {}: {}",
                        evenement.getEventId(), e.getMessage(), e);
            }
        }

        log.info("✅ Batch terminé: {} succès, {} échecs, {} skippés", succes, echecs, skips);

        // Réinitialiser les événements bloqués dans sa propre transaction
        eventTransactionProcessor.reinitialiserEvenementsBloques();
    }

    /**
     * Retraitement manuel d'un événement spécifique (appelé depuis EventAdminResource).
     *
     * ✅ Trouve les handlers en mémoire + délègue à EventTransactionProcessor (REQUIRES_NEW)
     * ✅ Le claim atomique dans processerEvenement() protège contre les doublons
     *
     * @param eventId UUID de l'événement à retraiter
     * @return true si traité, false si skipé (déjà pris ou inconnu)
     */
    public boolean retraiterEvenement(UUID eventId) {
        // Lecture hors transaction pour obtenir les infos de base
        var evenementOpt = eventRecordRepository.findById(eventId);
        if (evenementOpt.isEmpty()) {
            log.warn("⚠️ Événement {} introuvable pour retraitement", eventId);
            return false;
        }

        var evenement = evenementOpt.get();
        List<EventTransactionProcessor.HandlerRegistre> handlers = trouverHandlersCompatibles(evenement);

        if (handlers.isEmpty()) {
            log.warn("⚠️ Aucun handler compatible pour retraiter l'événement {} (type={})",
                    eventId, evenement.getEventType());
        }

        return eventTransactionProcessor.processerEvenement(eventId, handlers, objectMapper);
    }

    /**
     * Trouve les handlers compatibles pour un événement (recherche en mémoire).
     */
    private List<EventTransactionProcessor.HandlerRegistre> trouverHandlersCompatibles(
            EventRecordJpa evenement) {

        List<EventTransactionProcessor.HandlerRegistre> compatibles = new ArrayList<>();

        for (EventTransactionProcessor.HandlerRegistre handler : handlersRegistres) {
            if (handler.getTypeEvent().getSimpleName().equals(evenement.getEventType())
                    || handler.getTypeEvent().equals(Object.class)) {
                compatibles.add(handler);
            }
        }

        log.debug("Trouvé {} handlers compatibles pour le type: {}",
                compatibles.size(), evenement.getEventType());
        return compatibles;
    }
}
