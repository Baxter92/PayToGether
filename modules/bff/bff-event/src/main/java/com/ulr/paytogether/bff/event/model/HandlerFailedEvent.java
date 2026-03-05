package com.ulr.paytogether.bff.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Événement émis quand un handler a échoué dans le traitement d'un événement.
 * Cet événement DOIT être publié à la fin de chaque traitement échoué.
 */
@Getter
@NoArgsConstructor
public class HandlerFailedEvent extends DomainEvent {

    /**
     * UUID de l'événement original qui a échoué
     */
    private UUID originalEventId;

    /**
     * Type de l'événement original
     */
    private String originalEventType;

    /**
     * Nom du handler qui a échoué
     */
    private String handlerName;

    /**
     * Message d'erreur
     */
    private String errorMessage;

    /**
     * Nom de la classe d'exception
     */
    private String exceptionClass;

    /**
     * Numéro de tentative qui a échoué
     */
    private Integer attemptNumber;

    /**
     * Indique si c'est l'échec final (max tentatives atteint)
     */
    private Boolean isFinalFailure;

    @JsonCreator
    public HandlerFailedEvent(
            @JsonProperty("originalEventId") UUID originalEventId,
            @JsonProperty("originalEventType") String originalEventType,
            @JsonProperty("handlerName") String handlerName,
            @JsonProperty("errorMessage") String errorMessage,
            @JsonProperty("exceptionClass") String exceptionClass,
            @JsonProperty("attemptNumber") Integer attemptNumber,
            @JsonProperty("isFinalFailure") Boolean isFinalFailure) {
        super("HandlerExecutionTracker");
        this.originalEventId = originalEventId;
        this.originalEventType = originalEventType;
        this.handlerName = handlerName;
        this.errorMessage = errorMessage;
        this.exceptionClass = exceptionClass;
        this.attemptNumber = attemptNumber;
        this.isFinalFailure = isFinalFailure;
    }

    @Override
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing HandlerFailedEvent", e);
        }
    }
}

