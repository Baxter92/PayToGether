package com.ulr.paytogether.configuration.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration du cache Caffeine pour l'application PayToGether
 *
 * Stratégie de cache :
 * - deals : TTL 5 minutes, max 1000 entrées
 * - deal-statut : TTL 3 minutes, max 500 entrées
 * - deal-createur : TTL 3 minutes, max 500 entrées
 * - deal-categorie : TTL 3 minutes, max 500 entrées
 * - villes : TTL 10 minutes, max 100 entrées
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "deals",           // Cache pour lireTous()
            "deal-uuid",       // Cache pour lireParUuid()
            "deal-statut",     // Cache pour lireParStatut()
            "deal-createur",   // Cache pour lireParCreateur()
            "deal-categorie",  // Cache pour lireParCategorie()
            "villes"           // Cache pour lireVillesDisponibles()
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configuration Caffeine avec :
     * - Expiration après 5 minutes (TTL)
     * - Taille maximale de 1000 entrées
     * - Enregistrement des statistiques pour monitoring
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)   // TTL de 5 minutes
            .maximumSize(1000)                        // Max 1000 entrées
            .recordStats();                           // Activer les stats de cache
    }
}

