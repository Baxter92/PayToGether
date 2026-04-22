package com.ulr.paytogether.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.extern.slf4j.Slf4j;
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

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Configuration du cache Redis.
 *
 * ✅ Force RESP2 pour éviter "NOAUTH HELLO" avec Redis 7 + Lettuce.
 *
 * ✅ SÉRIALISATION SÛRE — Évite LazyInitializationException lors de la sérialisation/désérialisation :
 *    - Mapping abstrait : PersistentBag/PersistentList → ArrayList
 *    - PersistentSet → HashSet
 *    Permet de lire les stale data Redis déjà corrompues SANS session Hibernate.
 *
 * ✅ Préfixe versionné ("v2:") : invalide automatiquement tout le cache existant
 *    lors du déploiement pour repartir sur des données propres.
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig {

    @Value("${spring.data.redis.host:redis-service}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

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
     * Connexion Lettuce avec RESP2 forcé.
     * Évite l'erreur "NOAUTH HELLO must be called with the client already authenticated"
     * causée par Lettuce 6+ qui envoie HELLO 3 (RESP3) avant AUTH avec Redis 7.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (StringUtils.hasText(redisPassword)) {
            redisConfig.setPassword(redisPassword);
        }

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
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(buildObjectMapper());

        // ✅ Préfixe "v2:" → invalide automatiquement toutes les stale data de la version précédente
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("v2:")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

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

    /**
     * ObjectMapper configuré pour Redis :
     *
     * ✅ activateDefaultTyping(NON_FINAL) : conserve les informations de type pour la désérialisation
     *    des modèles polymorphiques (Optional, etc.)
     *
     * ✅ Mapping Hibernate → Java standard :
     *    - PersistentBag / PersistentList → ArrayList
     *    - PersistentSet → HashSet
     *    Permet de LIRE les anciennes clés Redis corrompues (@class:PersistentBag)
     *    SANS session Hibernate. Évite LazyInitializationException.
     *
     * ✅ TypeResolverBuilder personnalisé : exclut org.hibernate.* des métadonnées @class
     *    pour les FUTURES sérialisations (le mapper fix dans DealJpaMapper prévient aussi).
     */
    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ Mapper Hibernate collections → Java standard lors de la DÉsérialisation
        // Traite les stale data Redis déjà corrompues avec @class: PersistentBag
        mapperHibernateVersJava(objectMapper);

        // ✅ TypeResolver personnalisé : exclut les types Hibernate de la sérialisation @class
        objectMapper.setDefaultTyping(
                new HibernateSafeTypeResolverBuilder(LaissezFaireSubTypeValidator.instance)
        );

        return objectMapper;
    }

    /**
     * Mappe les types Hibernate vers leurs équivalents Java standard via un SimpleModule.
     * Utilise Class.forName pour ne pas forcer la dépendance Hibernate dans les imports.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mapperHibernateVersJava(ObjectMapper objectMapper) {
        SimpleModule hibernateModule = new SimpleModule("HibernateCollectionSafeModule");

        String[] persistentListTypes = {
                "org.hibernate.collection.internal.PersistentBag",
                "org.hibernate.collection.internal.PersistentList",
                "org.hibernate.collection.spi.PersistentBag",
                "org.hibernate.collection.spi.PersistentList"
        };
        String[] persistentSetTypes = {
                "org.hibernate.collection.internal.PersistentSet",
                "org.hibernate.collection.spi.PersistentSet"
        };

        for (String className : persistentListTypes) {
            try {
                Class hibernateType = Class.forName(className);
                hibernateModule.addAbstractTypeMapping(hibernateType, ArrayList.class);
                log.debug("✅ Hibernate type mappé → ArrayList: {}", className);
            } catch (ClassNotFoundException ignored) {
                // Type non présent sur le classpath de cette version d'Hibernate, ignoré
            }
        }
        for (String className : persistentSetTypes) {
            try {
                Class hibernateType = Class.forName(className);
                hibernateModule.addAbstractTypeMapping(hibernateType, HashSet.class);
                log.debug("✅ Hibernate type mappé → HashSet: {}", className);
            } catch (ClassNotFoundException ignored) {
                // Type non présent sur le classpath de cette version d'Hibernate, ignoré
            }
        }

        objectMapper.registerModule(hibernateModule);
    }

    /**
     * TypeResolverBuilder qui exclut les types Hibernate internes de la sérialisation @class.
     * Évite que Jackson écrive "@class: PersistentBag" dans Redis pour les futures sérialisations.
     */
    private static class HibernateSafeTypeResolverBuilder
            extends ObjectMapper.DefaultTypeResolverBuilder {

        HibernateSafeTypeResolverBuilder(LaissezFaireSubTypeValidator validator) {
            super(ObjectMapper.DefaultTyping.NON_FINAL, validator);
            init(JsonTypeInfo.Id.CLASS, null);
            inclusion(JsonTypeInfo.As.PROPERTY);
        }

        @Override
        public boolean useForType(com.fasterxml.jackson.databind.JavaType t) {
            String className = t.getRawClass().getName();
            // ✅ Exclure les types Hibernate : ils ne doivent jamais être sérialisés
            //    avec leur @class dans Redis (nécessitent une session Hibernate)
            if (className.startsWith("org.hibernate.collection")
                    || className.startsWith("org.hibernate.proxy")) {
                return false;
            }
            return super.useForType(t);
        }
    }
}
