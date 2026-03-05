package com.ulr.paytogether.bff.event.handler;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Interface de base pour tous les handlers d'événements.
 *
 * Chaque handler doit implémenter cette interface pour pouvoir consommer
 * les événements publiés par le module core.
 *
 * ⚠️ STRATÉGIE DE RETRY AUTOMATIQUE :
 * Tous les handlers héritent automatiquement d'une stratégie de retry avec backoff exponentiel :
 * - Nombre maximum de tentatives : 3
 * - Délai initial : 1 seconde
 * - Facteur multiplicatif : 1.5 (backoff exponentiel)
 * - Délai maximum : 30 secondes
 * - Jitter : activé (pour éviter les collisions)
 *
 * Séquence de retry :
 * - Tentative 1 : immédiat
 * - Tentative 2 : ~1 seconde (+ jitter aléatoire)
 * - Tentative 3 : ~1.5 secondes (+ jitter aléatoire)
 *
 * Si vous avez besoin d'une configuration différente pour un handler spécifique,
 * vous pouvez surcharger @Retryable sur la méthode du handler.
 */
@Retryable(
    retryFor = Exception.class,
    maxAttempts = 3,
    backoff = @Backoff(
        delay = 1000,           // 1 seconde
        multiplier = 1.5,       // Facteur 1.5 (backoff exponentiel)
        maxDelay = 30000,       // Max 30 secondes
        random = true           // Jitter activé
    )
)
public interface ConsumerHandler {
    // Marker interface - tous les handlers doivent l'implémenter
}

