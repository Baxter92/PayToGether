package com.ulr.paytogether.bff.event.model;

/**
 * Interface pour le dispatcher d'événements.
 * Cette interface sera implémentée dans le module bff-event-dispatcher.
 */
public interface EventDispatcher {

    /**
     * Dispatch un événement vers le système de gestion d'événements
     *
     * @param event L'événement à dispatcher
     */
    void dispatch(DomainEvent event);

    /**
     * Dispatch un événement de manière asynchrone
     *
     * @param event L'événement à dispatcher
     */
    void dispatchAsync(DomainEvent event);
}

