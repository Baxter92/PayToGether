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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsable de la consommation des événements.
 * Utilise un scheduler pour traiter les événements en attente.
 */
@Service
@Slf4j
public class EventConsumerService {

    private final EventRecordRepository eventRecordRepository;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final List<HandlerInfo> registeredHandlers = new ArrayList<>();

    @Autowired
    public EventConsumerService(EventRecordRepository eventRecordRepository,
                               ApplicationContext applicationContext) {
        this.eventRecordRepository = eventRecordRepository;
        this.applicationContext = applicationContext;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        discoverHandlers();
    }

    /**
     * Découvre tous les handlers annotés avec @FunctionalHandler
     */
    private void discoverHandlers() {
        log.info("Discovering event handlers...");

        String[] beanNames = applicationContext.getBeanNamesForType(ConsumerHandler.class);
        log.info("Found {} beans implementing ConsumerHandler", beanNames.length);

        for (String beanName : beanNames) {
            log.info("Scanning bean: {}", beanName);
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();

            log.info("Bean class: {}", beanClass.getName());
            log.info("Bean superclass: {}", beanClass.getSuperclass().getName());

            // Scanner la classe réelle (pas le proxy)
            Class<?> targetClass = org.springframework.aop.support.AopUtils.getTargetClass(bean);
            log.info("Target class (unwrapped): {}", targetClass.getName());

            Method[] methods = targetClass.getDeclaredMethods();
            log.info("Found {} methods in target class", methods.length);

            for (Method method : methods) {
                log.debug("Checking method: {}", method.getName());

                if (method.isAnnotationPresent(FunctionalHandler.class)) {
                    FunctionalHandler annotation = method.getAnnotation(FunctionalHandler.class);

                    HandlerInfo handlerInfo = new HandlerInfo(
                            bean,
                            method,
                            annotation.eventType(),
                            annotation.maxAttempts(),
                            targetClass.getSimpleName() + "." + method.getName()
                    );

                    registeredHandlers.add(handlerInfo);
                    log.info("✅ Registered handler: {} for event type: {} (class: {})",
                            handlerInfo.getHandlerName(),
                            handlerInfo.getEventType().getSimpleName(),
                            handlerInfo.getEventType().getName());
                }
            }
        }

        log.info("========================================");
        log.info("Total handlers registered: {}", registeredHandlers.size());
        log.info("========================================");

        if (registeredHandlers.isEmpty()) {
            log.warn("⚠️  NO HANDLERS REGISTERED! Check if bff-event module is properly scanned.");
        }
    }

    /**
     * Traite les événements en attente toutes les 60 secondes.
     * Délai augmenté à 60 secondes pour :
     * - Éviter les doublons d'emails
     * - Donner le temps aux événements en PROCESSING de se terminer
     * - Réduire la charge sur le système
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 15000)
    @Transactional
    public void processePendingEvents() {
        List<EventRecordJpa> pendingEvents = eventRecordRepository.findByStatus(EventRecordJpa.EventStatus.PENDING);

        if (!pendingEvents.isEmpty()) {
            log.info("⚙️ Processing {} pending events", pendingEvents.size());

            for (EventRecordJpa eventRecord : pendingEvents) {
                processEvent(eventRecord);
            }
        }

        // Réinitialiser les événements bloqués (en traitement depuis plus de 10 minutes)
        // Augmenté de 5 à 10 minutes pour éviter les réinitialisations prématurées
        resetStuckEvents();
    }

    /**
     * Traite un événement spécifique.
     * Vérifie que l'événement n'est pas déjà en traitement avant de continuer.
     */
    @Transactional
    public void processEvent(EventRecordJpa eventRecord) {
        log.debug("Processing event: {} of type: {}", eventRecord.getEventId(), eventRecord.getEventType());

        // ✅ PROTECTION CONTRE LES DOUBLONS : Vérifier que l'événement est toujours PENDING
        // Il pourrait avoir été pris par un autre thread entre-temps
        EventRecordJpa freshEventRecord = eventRecordRepository.findById(eventRecord.getEventId()).orElse(null);
        if (freshEventRecord == null) {
            log.warn("⚠️ Event {} not found in database, skipping", eventRecord.getEventId());
            return;
        }

        if (freshEventRecord.getStatus() != EventRecordJpa.EventStatus.PENDING) {
            log.warn("⚠️ Event {} is already {} (not PENDING), skipping to avoid duplicate processing",
                    freshEventRecord.getEventId(), freshEventRecord.getStatus());
            return;
        }

        // Marquer comme en traitement
        freshEventRecord.setStatus(EventRecordJpa.EventStatus.PROCESSING);
        freshEventRecord.setLastAttemptAt(LocalDateTime.now());
        eventRecordRepository.saveAndFlush(freshEventRecord); // Force flush pour garantir le commit immédiat

        try {
            // Trouver les handlers compatibles
            List<HandlerInfo> compatibleHandlers = findCompatibleHandlers(freshEventRecord);

            if (compatibleHandlers.isEmpty()) {
                log.warn("No handler found for event type: {}", freshEventRecord.getEventType());
                markAsFailed(freshEventRecord, "No compatible handler found");
                return;
            }

            // Désérialiser l'événement
            Object event = deserializeEvent(freshEventRecord);

            // Exécuter tous les handlers compatibles
            boolean allSuccess = true;
            StringBuilder errors = new StringBuilder();

            for (HandlerInfo handlerInfo : compatibleHandlers) {
                try {
                    executeHandler(handlerInfo, event, freshEventRecord);
                    log.info("Handler {} executed successfully for event {}",
                            handlerInfo.getHandlerName(), freshEventRecord.getEventId());
                } catch (Exception e) {
                    allSuccess = false;
                    errors.append(handlerInfo.getHandlerName())
                          .append(": ")
                          .append(e.getMessage())
                          .append("; ");
                    log.error("Error executing handler {} for event {}: {}",
                            handlerInfo.getHandlerName(), freshEventRecord.getEventId(), e.getMessage(), e);
                }
            }

            if (allSuccess) {
                markAsConsumed(freshEventRecord, compatibleHandlers.get(0).getHandlerName());
            } else {
                handleFailure(freshEventRecord, errors.toString());
            }

        } catch (Exception e) {
            log.error("Error processing event {}: {}", freshEventRecord.getEventId(), e.getMessage(), e);
            handleFailure(freshEventRecord, e.getMessage());
        }
    }

    /**
     * Trouve les handlers compatibles pour un événement
     */
    private List<HandlerInfo> findCompatibleHandlers(EventRecordJpa eventRecord) {
        List<HandlerInfo> compatible = new ArrayList<>();

        log.debug("Searching for handlers for event type: {}", eventRecord.getEventType());
        log.debug("Total registered handlers: {}", registeredHandlers.size());

        for (HandlerInfo handlerInfo : registeredHandlers) {
            log.debug("Checking handler: {} with event type: {}",
                    handlerInfo.getHandlerName(),
                    handlerInfo.getEventType().getSimpleName());

            if (handlerInfo.getEventType().getSimpleName().equals(eventRecord.getEventType()) ||
                handlerInfo.getEventType().equals(Object.class)) {
                compatible.add(handlerInfo);
                log.debug("Handler {} is compatible!", handlerInfo.getHandlerName());
            }
        }

        log.info("Found {} compatible handlers for event type: {}", compatible.size(), eventRecord.getEventType());
        return compatible;
    }

    /**
     * Désérialise un événement depuis JSON
     */
    private Object deserializeEvent(EventRecordJpa eventRecord) throws Exception {
        // Trouver la classe de l'événement
        Class<?> eventClass = findEventClass(eventRecord.getEventType());
        return objectMapper.readValue(eventRecord.getPayload(), eventClass);
    }

    /**
     * Trouve la classe d'événement par son nom
     */
    @SuppressWarnings("unchecked")
    private Class<?> findEventClass(String eventType) throws ClassNotFoundException {
        // Rechercher dans le package du core
        String corePackage = "com.ulr.paytogether.core.event";

        try {
            return Class.forName(corePackage + "." + eventType);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Event class not found: " + eventType + " in package " + corePackage);
        }
    }

    /**
     * Exécute un handler avec l'événement
     */
    private void executeHandler(HandlerInfo handlerInfo, Object event, EventRecordJpa eventRecord) throws Exception {
        Method method = handlerInfo.getMethod();
        method.setAccessible(true);
        method.invoke(handlerInfo.getHandlerInstance(), event);

        eventRecord.setConsumerHandler(handlerInfo.getHandlerName());

        // ❌ NE PLUS PUBLIER HandlerConsumedEvent pour éviter la boucle infinie et les doublons
        // Les logs suffisent pour tracer la consommation réussie
        log.info("✅ Handler {} successfully consumed event {} of type {}",
                handlerInfo.getHandlerName(), eventRecord.getEventId(), eventRecord.getEventType());
    }


    /**
     * Marque un événement comme consommé
     */
    private void markAsConsumed(EventRecordJpa eventRecord, String handlerName) {
        eventRecord.setStatus(EventRecordJpa.EventStatus.CONSUMED);
        eventRecord.setConsumedAt(LocalDateTime.now());
        eventRecord.setConsumerHandler(handlerName);
        eventRecordRepository.save(eventRecord);

        log.info("Event {} marked as consumed by {}", eventRecord.getEventId(), handlerName);
    }

    /**
     * Marque un événement comme échoué
     */
    private void markAsFailed(EventRecordJpa eventRecord, String errorMessage) {
        eventRecord.setStatus(EventRecordJpa.EventStatus.FAILED);
        eventRecord.setFailedAt(LocalDateTime.now());
        eventRecord.setErrorMessage(errorMessage);
        eventRecordRepository.save(eventRecord);

        log.error("Event {} marked as failed: {}", eventRecord.getEventId(), errorMessage);
    }

    /**
     * Gère l'échec d'un événement avec retry
     */
    private void handleFailure(EventRecordJpa eventRecord, String errorMessage) {
        eventRecord.setAttempts(eventRecord.getAttempts() + 1);
        eventRecord.setErrorMessage(errorMessage);

        boolean isFinalFailure = eventRecord.getAttempts() >= eventRecord.getMaxAttempts();

        // ❌ NE PLUS PUBLIER HandlerFailedEvent pour éviter la boucle infinie et les doublons
        // Les logs suffisent pour tracer les échecs
        log.error("❌ Handler failed for event {} of type {} (attempt {}/{}, final={}): {}",
                eventRecord.getEventId(), eventRecord.getEventType(),
                eventRecord.getAttempts(), eventRecord.getMaxAttempts(),
                isFinalFailure, errorMessage);

        if (isFinalFailure) {
            markAsFailed(eventRecord, "Max attempts reached: " + errorMessage);
        } else {
            eventRecord.setStatus(EventRecordJpa.EventStatus.PENDING);
            eventRecordRepository.save(eventRecord);
            log.warn("Event {} failed, will retry. Attempt {}/{}",
                    eventRecord.getEventId(), eventRecord.getAttempts(), eventRecord.getMaxAttempts());
        }
    }


    /**
     * Réinitialise les événements bloqués en traitement.
     * Réinitialise les événements en PROCESSING depuis plus de 10 minutes.
     */
    private void resetStuckEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<EventRecordJpa> stuckEvents = eventRecordRepository.findStuckEvents(threshold);

        if (!stuckEvents.isEmpty()) {
            log.warn("⚠️ Found {} stuck events, resetting to PENDING", stuckEvents.size());

            for (EventRecordJpa event : stuckEvents) {
                event.setStatus(EventRecordJpa.EventStatus.PENDING);
                eventRecordRepository.save(event);
            }
        }
    }

    /**
     * Classe interne pour stocker les informations d'un handler
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class HandlerInfo {
        private Object handlerInstance;
        private Method method;
        private Class<?> eventType;
        private int maxAttempts;
        private String handlerName;
    }
}

