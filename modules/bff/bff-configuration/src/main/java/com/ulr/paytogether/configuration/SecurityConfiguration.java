package com.ulr.paytogether.configuration;

import com.ulr.paytogether.configuration.security.DynamicRealmJwtDecoder;
import com.ulr.paytogether.configuration.security.JwtConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la sécurité
 * Fournit le bean BCryptPasswordEncoder pour le hachage des mots de passe
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final JwtConverter jwtConverter;
    private final DynamicRealmJwtDecoder jwtDecoder;

    /**
     * Configuration de la chaîne de filtres de sécurité
     * - CORS activé avec les paramètres par défaut
     * - CSRF désactivé (car c'est une API REST)
     * - Endpoints publics pour /api/public/**, /api/auth/**, /actuator/**, /swagger-ui/** et /v3/api-docs/**
     * - Tous les autres endpoints nécessitent une authentification
     * - Session stateless (pas de session côté serveur)
     * - OAuth2 Resource Server configuré pour utiliser JWT avec un Bearer Token Resolver personnalisé
     *
     * @param http HttpSecurity pour configurer la sécurité
     * @return SecurityFilterChain configurée
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuration de la chaîne de filtres de sécurité");

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints publics
                        .requestMatchers("/api/public/**", "/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/utilisateurs").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Tous les autres endpoints nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            String path = request.getRequestURI();
                            // Ne pas extraire le token pour les endpoints publics
                            if (path.contains("/api/public/") || path.contains("/api/auth/")) {
                                return null;
                            }
                            return new DefaultBearerTokenResolver().resolve(request);
                        })
                        .jwt(jwt -> {
                            jwt.decoder(jwtDecoder);
                            jwt.jwtAuthenticationConverter(jwtConverter);
                        })
                )
                .build();
    }

    /**
     * Bean BCryptPasswordEncoder pour hacher les mots de passe
     * Utilise le coût par défaut de 10 rounds
     *
     * @return instance de BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

