package com.ulr.paytogether.configuration.tomcat;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * Configuration de Tomcat pour utiliser les Virtual Threads (Java 21)
 *
 * Avantages :
 * - Gère des milliers de requêtes concurrentes avec peu de ressources
 * - Réduit la latence des requêtes I/O bloquantes (DB, MinIO, API externes)
 * - Pas de limite de threads à configurer
 */
@Configuration
public class TomcatVirtualThreadConfig {

    /**
     * Active les Virtual Threads pour le gestionnaire de protocole Tomcat
     * Chaque requête HTTP sera traitée dans un Virtual Thread
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}

