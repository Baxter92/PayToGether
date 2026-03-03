package com.ulr.paytogether.bff.event.handler;

import com.ulr.paytogether.bff.event.model.DomainEvent;

/**
 * Interface de base pour tous les handlers d'événements.
 * Chaque handler doit implémenter cette interface pour pouvoir consommer
 * les événements publiés par le module core.
 */
public interface ConsumerHandler {
    /**
     * Détermine si ce handler peut traiter l'événement donné
     *
     * @param event L'événement à vérifier
     * @return true si le handler peut traiter cet événement
     */
    default boolean canHandle(DomainEvent event) {
        return true;
    }

    /**
     * Retourne le type d'événement que ce handler peut traiter
     *
     * @return La classe de l'événement
     */
    default Class<? extends DomainEvent> getEventType() {
        return DomainEvent.class;
    }
}

