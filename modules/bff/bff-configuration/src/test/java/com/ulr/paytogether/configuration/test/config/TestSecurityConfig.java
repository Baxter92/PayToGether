package com.ulr.paytogether.configuration.test.config;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Configuration de sécurité pour les tests d'intégration
 * Remplace la configuration de production avec une sécurité permissive
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Filter chain de test qui remplace celui de production
     * Utilise @Order(0) pour être prioritaire sur la configuration standard (ordre par défaut = Integer.MAX_VALUE)
     */
    @Bean
    @Primary
    @Order(0)  // ✅ Order(0) pour avoir la priorité absolue
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/**")  // ✅ Matcher explicite pour éviter les conflits
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()  // ✅ Tous les endpoints sont accessibles sans authentification
                )
                .build();
    }

    /**
     * JwtDecoder mocké pour les tests
     * Retourne un JWT valide avec un rôle ADMIN pour tous les tokens
     */
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "test-user-123")
                .claim("preferred_username", "test-admin")
                .claim("email", "test@paytogether.com")
                .claim("scope", "ROLE_ADMIN ROLE_VENDEUR ROLE_UTILISATEUR")
                .claim("realm_access", Map.of("roles", List.of("ADMIN", "VENDEUR", "UTILISATEUR")))
                .build();
    }

    /**
     * Bean BCryptPasswordEncoder pour les tests
     * Remplace celui de SecurityConfiguration qui est désactivé en mode test
     */
    @Bean
    @Primary
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    MinioClient minioClient() {
        MinioClient client = Mockito.mock(MinioClient.class);

        try {
            Mockito.when(client.getPresignedObjectUrl(Mockito.any()))
                    .thenReturn("http://localhost:9999/fake-upload-url");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return client;
    }

}

