package com.ulr.paytogether.bff.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer une méthode comme handler fonctionnel d'événements.
 * Les méthodes annotées avec @FunctionalHandler peuvent consommer les événements
 * publiés par le module core avec une logique de retry automatique.
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
     */
    int maxAttempts() default 3;

    /**
     * Description du handler
     */
    String description() default "";
}

