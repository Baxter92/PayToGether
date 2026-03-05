package com.ulr.paytogether.bff.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer une méthode comme handler fonctionnel d'événements.
 * Les méthodes annotées avec @FunctionalHandler peuvent consommer les événements
 * publiés par le module core avec une logique de retry automatique.
 *
 * ⚠️ IMPORTANT : Le retry est géré automatiquement par Spring @Retryable.
 * La stratégie de backoff exponentiel est configurée dans la classe handler avec :
 *
 * <pre>
 * {@code
 * @Retryable(
 *     retryFor = Exception.class,
 *     maxAttempts = 3,
 *     backoff = @Backoff(
 *         delay = 1000,           // 1 seconde
 *         multiplier = 1.5,       // Facteur 1.5
 *         maxDelay = 30000,       // Max 30 secondes
 *         random = true           // Jitter activé
 *     )
 * )
 * }
 * </pre>
 *
 * Exemple d'utilisation :
 * <pre>
 * {@code
 * @Component
 * @RequiredArgsConstructor
 * public class AccountValidationHandler implements ConsumerHandler {
 *     private final ValidationTokenService validationTokenService;
 *
 *     @FunctionalHandler(
 *         eventType = AccountValidationEvent.class,
 *         maxAttempts = 3
 *     )
 *     @Retryable(
 *         retryFor = Exception.class,
 *         maxAttempts = 3,
 *         backoff = @Backoff(delay = 1000, multiplier = 1.5, maxDelay = 30000)
 *     )
 *     public void handleAccountValidation(AccountValidationEvent event) {
 *         validationTokenService.creer(...);
 *     }
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionalHandler {
    /**
     * Type d'événement que ce handler peut consommer
     */
    Class<?> eventType() default Object.class;

    /**
     * Nombre maximum de tentatives de traitement
     * Par défaut : 3 tentatives
     */
    int maxAttempts() default 3;


    /**
     * Description du handler
     */
    String description() default "";
}

