package com.ulr.paytogether.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Événement émis quand un handler a consommé avec succès un événement.
 * Cet événement DOIT être publié à la fin de chaque traitement réussi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandlerConsumedEvent {

    /**
     * ID de l'événement original qui a été consommé
     */
    private String eventId;

    /**
     * Type de l'événement original
     */
    private String eventType;

    /**
     * Nom du handler qui a consommé l'événement
     */
    private String handlerName;

    /**
     * Message de confirmation (optionnel)
     */
    private String message;
}

