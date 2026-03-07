package com.ulr.paytogether.bff.event.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration pour le module bff-event.
 * Active le support de @Retryable pour les handlers d'événements.
 */
@Configuration
@EnableRetry
@ComponentScan(basePackages = "com.ulr.paytogether.bff.event")
public class EventConfiguration {
}

