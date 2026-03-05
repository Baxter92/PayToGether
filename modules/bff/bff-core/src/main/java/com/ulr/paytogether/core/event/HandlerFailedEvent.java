package com.ulr.paytogether.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Événement émis quand un handler a échoué à traiter un événement.
 * Permet de tracer les échecs et de mettre en place des mécanismes de compensation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandlerFailedEvent {

    /**
     * ID de l'événement original qui a échoué
     */
    private String eventId;

    /**
     * Type de l'événement original
     */
    private String eventType;

    /**
     * Nom du handler qui a échoué
     */
    private String handlerName;

    /**
     * Message d'erreur
     */
    private String errorMessage;

    /**
     * Classe de l'exception
     */
    private String exceptionClass;

    /**
     * Numéro de la tentative
     */
    private Integer attemptNumber;

    /**
     * Indique si c'est l'échec final (plus de retry)
     */
    private Boolean isFinalFailure;
}

