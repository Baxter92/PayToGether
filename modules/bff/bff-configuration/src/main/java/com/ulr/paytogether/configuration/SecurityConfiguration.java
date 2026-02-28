package com.ulr.paytogether.configuration;

import com.ulr.paytogether.configuration.security.DynamicRealmJwtDecoder;
import com.ulr.paytogether.configuration.security.JwtConverter;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
    private final CorsProperties corsProperties;

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
                        .requestMatchers("/api/public/**", "/api/auth/login").permitAll()
                        .requestMatchers("/api/public/**", "/api/auth/register").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/deals/statut/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/deals/villes").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/deals/*/images/*/url").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/categories").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Tous les autres endpoints nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Ne pas retourner 401 pour les endpoints publics
                            if (isPublicEndpoint(request.getRequestURI())) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                return;
                            }
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Non autorisé\"}");
                        })
                        .bearerTokenResolver(request -> {
                            // Ne pas extraire le token pour les endpoints publics
                            if (isPublicEndpoint(request.getRequestURI())) {
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
     * Vérifie si un endpoint est public (ne nécessite pas d'authentification)
     *
     * @param path le chemin de la requête
     * @return true si l'endpoint est public, false sinon
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/public/") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/deals/statut") ||
               path.startsWith("/api/deals/villes") ||
               (path.startsWith("/api/deals/") && path.contains("/images/") && path.endsWith("/url")) ||
               path.startsWith("/api/categories") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/");
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuration CORS");

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setAllowCredentials(corsProperties.isAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("CORS configuré - Origins: {}, Methods: {}",
                corsProperties.getAllowedOrigins(),
                corsProperties.getAllowedMethods());

        return source;
    }
}
