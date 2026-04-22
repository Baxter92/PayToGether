package com.ulr.paytogether.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration du cache Redis.
 * Utilise la serialisation JSON (Jackson) pour stocker les objets.
 * Redis est optionnel : si Redis est down, Spring Cache se degrade gracieusement.
 * Les TTL sont configurables via application.properties (cache.ttl.xxx en secondes).
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    // TTL configurables par application.properties
    @Value("${cache.ttl.categories:1800}")   // 30 min
    private long ttlCategories;

    @Value("${cache.ttl.deals:1800}")        // 30 min
    private long ttlDeals;

    @Value("${cache.ttl.deal:1800}")         // 30 min
    private long ttlDeal;

    @Value("${cache.ttl.publicites:1800}")   // 30 min
    private long ttlPublicites;

    @Value("${cache.ttl.utilisateur:600}")   // 10 min
    private long ttlUtilisateur;

    @Value("${cache.ttl.commentaires:180}")  // 3 min
    private long ttlCommentaires;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Serializer JSON avec type info pour deserialiser les bons types polymorphiques
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Configuration par defaut (TTL = 5 min, utilisee si cache non declare explicitement)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues(); // Ne jamais cacher les null

        // TTL specifiques par cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("categories",   defaultConfig.entryTtl(Duration.ofSeconds(ttlCategories)));
        cacheConfigs.put("categorie",    defaultConfig.entryTtl(Duration.ofSeconds(ttlCategories)));
        cacheConfigs.put("deals",        defaultConfig.entryTtl(Duration.ofSeconds(ttlDeals)));
        cacheConfigs.put("deal",         defaultConfig.entryTtl(Duration.ofSeconds(ttlDeal)));
        cacheConfigs.put("publicites",   defaultConfig.entryTtl(Duration.ofSeconds(ttlPublicites)));
        cacheConfigs.put("utilisateur",  defaultConfig.entryTtl(Duration.ofSeconds(ttlUtilisateur)));
        cacheConfigs.put("commentaires", defaultConfig.entryTtl(Duration.ofSeconds(ttlCommentaires)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}

