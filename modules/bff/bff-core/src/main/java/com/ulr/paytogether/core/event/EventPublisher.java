package com.ulr.paytogether.core.event;

/**
 * Interface pour publier des événements du domaine (PORT)
 *
 * Cette interface fait partie du CORE (hexagone central).
 * Elle définit un PORT qui sera implémenté par un adaptateur (bff-event-dispatcher).
 *
 * Le core ne connaît QUE cette interface, jamais l'implémentation.
 *
 * Architecture :
 * - bff-core : Définit EventPublisher (ce fichier)
 * - bff-event-dispatcher : Implémente EventPublisher (EventDispatcherImpl)
 * - Services du core : Injectent EventPublisher pour publier des événements
 */
public interface EventPublisher {

    /**
     * Publie un événement de manière asynchrone
     * L'événement sera persisté en base de données puis traité par les handlers.
     *
     * @param event L'événement à publier (peut être n'importe quel objet)
     */
    void publishAsync(Object event);

    /**
     * Publie un événement de manière synchrone
     * L'événement sera persisté en base de données immédiatement.
     *
     * @param event L'événement à publier (peut être n'importe quel objet)
     */
    void publishSync(Object event);
}

