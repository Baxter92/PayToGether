package com.ulr.paytogether.configuration.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration des Virtual Threads (Java 21 Project Loom)
 *
 * Avantages :
 * - Légers (millions de threads possibles)
 * - Parfaits pour les I/O bloquantes (MinIO, JWT, DB)
 * - Pas de limite de pool à gérer
 * - Réduction de la latence sur les appels externes
 */
@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * Executor basé sur Virtual Threads pour les tâches asynchrones
     * Utilisé par @Async
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Executor pour les tâches gourmandes en CPU (calculs intensifs)
     * Utilise les threads platform classiques
     */
    @Bean(name = "platformThreadExecutor")
    public Executor platformThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("platform-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

