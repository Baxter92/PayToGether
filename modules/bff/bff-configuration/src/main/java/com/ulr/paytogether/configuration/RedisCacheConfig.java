package com.ulr.paytogether.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration du cache Redis.
 *
 * ✅ Force RESP2 (ProtocolVersion.RESP2) pour éviter le bug Lettuce / Redis 7 :
 *    "NOAUTH HELLO must be called with the client already authenticated"
 *    Lettuce 6+ envoie HELLO 3 (RESP3) avant AUTH → Redis 7 refuse si requirepass activé.
 *    En forçant RESP2, Lettuce envoie AUTH d'abord, puis communique en RESP2 classique.
 *
 * ✅ N'envoie pas AUTH si le mot de passe est vide (Redis sans auth en interne cluster).
 *
 * ✅ Redis optionnel : si Redis est down, Spring Cache se dégrade gracieusement.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.data.redis.host:redis-service}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    // TTL configurables via application.properties
    @Value("${cache.ttl.categories:1800}")
    private long ttlCategories;

    @Value("${cache.ttl.deals:1800}")
    private long ttlDeals;

    @Value("${cache.ttl.deal:1800}")
    private long ttlDeal;

    @Value("${cache.ttl.publicites:1800}")
    private long ttlPublicites;

    @Value("${cache.ttl.utilisateur:600}")
    private long ttlUtilisateur;

    @Value("${cache.ttl.commentaires:180}")
    private long ttlCommentaires;

    /**
     * Bean LettuceConnectionFactory configuré manuellement pour :
     * - Forcer RESP2 (évite le bug HELLO/NOAUTH avec Redis 7+)
     * - Ne pas envoyer AUTH si le mot de passe est vide
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Configuration standalone (host, port, password optionnel)
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

        // ✅ N'envoie AUTH que si le mot de passe est non-vide
        if (StringUtils.hasText(redisPassword)) {
            redisConfig.setPassword(redisPassword);
        }

        // ✅ Forcer RESP2 → Lettuce envoie AUTH avant tout, évite le bug HELLO/NOAUTH
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(ClientOptions.builder()
                        .protocolVersion(ProtocolVersion.RESP2)
                        .build())
                .commandTimeout(Duration.ofSeconds(3))
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Serializer JSON avec type info pour désérialiser les bons types
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

        // Configuration par défaut (TTL = 5 min)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // TTL spécifiques par cache
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
