package com.ulr.paytogether.bff.event.handler;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Interface de base pour tous les handlers d'événements.
 *
 * Chaque handler doit implémenter cette interface pour pouvoir consommer
 * les événements publiés par le module core.
 *
 * ⚠️ STRATÉGIE DE RETRY DÉSACTIVÉE :
 * Les retry automatiques sont DÉSACTIVÉS (maxAttempts = 1).
 * En cas d'échec, l'événement est immédiatement marqué comme FAILED.
 *
 * Les événements FAILED pourront être retraités plus tard via un batch manuel
 * ou un endpoint de retry.
 *
 * Avantages :
 * - Pas de doublons (emails, notifications, etc.)
 * - Contrôle total sur les retry
 * - Meilleure traçabilité des échecs
 *
 * Si vous avez besoin d'une configuration différente pour un handler spécifique,
 * vous pouvez surcharger @Retryable sur la méthode du handler.
 */
@Retryable(
    retryFor = Exception.class,
    maxAttempts = 1,  // ✅ UNE SEULE TENTATIVE - Pas de retry automatique
    backoff = @Backoff(delay = 0)  // Pas de délai
)
public interface ConsumerHandler {
    // Marker interface - tous les handlers doivent l'implémenter
}

