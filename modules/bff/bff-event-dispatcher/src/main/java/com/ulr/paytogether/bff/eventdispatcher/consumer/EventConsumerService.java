package com.ulr.paytogether.bff.eventdispatcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.bff.event.model.DomainEvent;
import com.ulr.paytogether.bff.event.model.HandlerConsumedEvent;
import com.ulr.paytogether.bff.event.model.HandlerFailedEvent;
import com.ulr.paytogether.bff.event.model.EventDispatcher;
import com.ulr.paytogether.bff.eventdispatcher.entity.EventRecordJpa;
import com.ulr.paytogether.bff.eventdispatcher.repository.EventRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class EventConsumerService {

    private final EventRecordRepository eventRecordRepository;
    private final ApplicationContext applicationContext;
    private final EventDispatcher eventDispatcher;
    private final ObjectMapper objectMapper;
    private final List<HandlerInfo> registeredHandlers = new ArrayList<>();

    public EventConsumerService(EventRecordRepository eventRecordRepository,
                               ApplicationContext applicationContext,
                               EventDispatcher eventDispatcher) {
        this.eventRecordRepository = eventRecordRepository;
        this.applicationContext = applicationContext;
        this.eventDispatcher = eventDispatcher;
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

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();

            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(FunctionalHandler.class)) {
                    FunctionalHandler annotation = method.getAnnotation(FunctionalHandler.class);

                    HandlerInfo handlerInfo = new HandlerInfo(
                            bean,
                            method,
                            annotation.eventType(),
                            annotation.maxAttempts(),
                            beanClass.getSimpleName() + "." + method.getName()
                    );

                    registeredHandlers.add(handlerInfo);
                    log.info("Registered handler: {} for event type: {}",
                            handlerInfo.getHandlerName(),
                            handlerInfo.getEventType().getSimpleName());
                }
            }
        }

        log.info("Total handlers registered: {}", registeredHandlers.size());
    }

    /**
     * Traite les événements en attente toutes les 5 secondes
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    @Transactional
    public void processePendingEvents() {
        List<EventRecordJpa> pendingEvents = eventRecordRepository.findByStatus(EventRecordJpa.EventStatus.PENDING);

        if (!pendingEvents.isEmpty()) {
            log.info("Processing {} pending events", pendingEvents.size());

            for (EventRecordJpa eventRecord : pendingEvents) {
                processEvent(eventRecord);
            }
        }

        // Réinitialiser les événements bloqués (en traitement depuis plus de 5 minutes)
        resetStuckEvents();
    }

    /**
     * Traite un événement spécifique
     */
    @Transactional
    public void processEvent(EventRecordJpa eventRecord) {
        log.debug("Processing event: {} of type: {}", eventRecord.getEventId(), eventRecord.getEventType());

        // Marquer comme en traitement
        eventRecord.setStatus(EventRecordJpa.EventStatus.PROCESSING);
        eventRecord.setLastAttemptAt(LocalDateTime.now());
        eventRecordRepository.save(eventRecord);

        try {
            // Trouver les handlers compatibles
            List<HandlerInfo> compatibleHandlers = findCompatibleHandlers(eventRecord);

            if (compatibleHandlers.isEmpty()) {
                log.warn("No handler found for event type: {}", eventRecord.getEventType());
                markAsFailed(eventRecord, "No compatible handler found");
                return;
            }

            // Désérialiser l'événement
            DomainEvent event = deserializeEvent(eventRecord);

            // Exécuter tous les handlers compatibles
            boolean allSuccess = true;
            StringBuilder errors = new StringBuilder();

            for (HandlerInfo handlerInfo : compatibleHandlers) {
                try {
                    executeHandler(handlerInfo, event, eventRecord);
                    log.info("Handler {} executed successfully for event {}",
                            handlerInfo.getHandlerName(), eventRecord.getEventId());
                } catch (Exception e) {
                    allSuccess = false;
                    errors.append(handlerInfo.getHandlerName())
                          .append(": ")
                          .append(e.getMessage())
                          .append("; ");
                    log.error("Error executing handler {} for event {}: {}",
                            handlerInfo.getHandlerName(), eventRecord.getEventId(), e.getMessage(), e);
                }
            }

            if (allSuccess) {
                markAsConsumed(eventRecord, compatibleHandlers.get(0).getHandlerName());
            } else {
                handleFailure(eventRecord, errors.toString());
            }

        } catch (Exception e) {
            log.error("Error processing event {}: {}", eventRecord.getEventId(), e.getMessage(), e);
            handleFailure(eventRecord, e.getMessage());
        }
    }

    /**
     * Trouve les handlers compatibles pour un événement
     */
    private List<HandlerInfo> findCompatibleHandlers(EventRecordJpa eventRecord) {
        List<HandlerInfo> compatible = new ArrayList<>();

        for (HandlerInfo handlerInfo : registeredHandlers) {
            if (handlerInfo.getEventType().getSimpleName().equals(eventRecord.getEventType()) ||
                handlerInfo.getEventType().equals(DomainEvent.class)) {
                compatible.add(handlerInfo);
            }
        }

        return compatible;
    }

    /**
     * Désérialise un événement depuis JSON
     */
    private DomainEvent deserializeEvent(EventRecordJpa eventRecord) throws Exception {
        // Trouver la classe de l'événement
        Class<? extends DomainEvent> eventClass = findEventClass(eventRecord.getEventType());
        return objectMapper.readValue(eventRecord.getPayload(), eventClass);
    }

    /**
     * Trouve la classe d'événement par son nom
     */
    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> findEventClass(String eventType) throws ClassNotFoundException {
        // Rechercher dans les packages connus
        String[] packages = {
            "com.ulr.paytogether.bff.event.model",
            "com.ulr.paytogether.bff.core.event"
        };

        for (String pkg : packages) {
            try {
                return (Class<? extends DomainEvent>) Class.forName(pkg + "." + eventType);
            } catch (ClassNotFoundException ignored) {
                // Continuer la recherche
            }
        }

        throw new ClassNotFoundException("Event class not found: " + eventType);
    }

    /**
     * Exécute un handler avec l'événement
     */
    private void executeHandler(HandlerInfo handlerInfo, DomainEvent event, EventRecordJpa eventRecord) throws Exception {
        Method method = handlerInfo.getMethod();
        method.setAccessible(true);
        method.invoke(handlerInfo.getHandlerInstance(), event);

        eventRecord.setConsumerHandler(handlerInfo.getHandlerName());

        // ✅ RÈGLE IMPORTANTE : Publier événement de confirmation après consommation réussie
        publishHandlerConsumedEvent(event, handlerInfo.getHandlerName());
    }

    /**
     * Publie un événement HandlerConsumedEvent pour tracer la consommation réussie
     */
    private void publishHandlerConsumedEvent(DomainEvent originalEvent, String handlerName) {
        try {
            HandlerConsumedEvent consumedEvent = new HandlerConsumedEvent(
                originalEvent.getEventId(),
                originalEvent.getEventType(),
                handlerName,
                "Handler executed successfully",
                null
            );

            eventDispatcher.dispatchAsync(consumedEvent);
            log.info("Published HandlerConsumedEvent for event {} by handler {}",
                    originalEvent.getEventId(), handlerName);

        } catch (Exception e) {
            log.error("Failed to publish HandlerConsumedEvent for event {}: {}",
                    originalEvent.getEventId(), e.getMessage(), e);
            // Ne pas relancer l'exception pour ne pas affecter le traitement principal
        }
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

        // ✅ RÈGLE IMPORTANTE : Publier événement d'échec
        publishHandlerFailedEvent(eventRecord, errorMessage, isFinalFailure);

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
     * Publie un événement HandlerFailedEvent pour tracer l'échec
     */
    private void publishHandlerFailedEvent(EventRecordJpa eventRecord, String errorMessage, boolean isFinalFailure) {
        try {
            // Extraire la classe d'exception du message d'erreur si possible
            String exceptionClass = extractExceptionClass(errorMessage);

            HandlerFailedEvent failedEvent = new HandlerFailedEvent(
                eventRecord.getEventId(),
                eventRecord.getEventType(),
                eventRecord.getConsumerHandler() != null ? eventRecord.getConsumerHandler() : "Unknown",
                errorMessage,
                exceptionClass,
                eventRecord.getAttempts() + 1,
                isFinalFailure
            );

            eventDispatcher.dispatchAsync(failedEvent);
            log.info("Published HandlerFailedEvent for event {} (attempt {}, final={})",
                    eventRecord.getEventId(), eventRecord.getAttempts() + 1, isFinalFailure);

        } catch (Exception e) {
            log.error("Failed to publish HandlerFailedEvent for event {}: {}",
                    eventRecord.getEventId(), e.getMessage(), e);
            // Ne pas relancer l'exception
        }
    }

    /**
     * Extrait le nom de la classe d'exception du message d'erreur
     */
    private String extractExceptionClass(String errorMessage) {
        if (errorMessage == null) return "UnknownException";

        // Pattern pour trouver le nom de classe Java (ex: "java.lang.NullPointerException: message")
        int colonIndex = errorMessage.indexOf(':');
        if (colonIndex > 0) {
            String potentialClass = errorMessage.substring(0, colonIndex).trim();
            if (potentialClass.contains(".")) {
                return potentialClass;
            }
        }

        return "UnknownException";
    }

    /**
     * Réinitialise les événements bloqués en traitement
     */
    private void resetStuckEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<EventRecordJpa> stuckEvents = eventRecordRepository.findStuckEvents(threshold);

        if (!stuckEvents.isEmpty()) {
            log.warn("Found {} stuck events, resetting to PENDING", stuckEvents.size());

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

