package com.ulr.paytogether.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration des clients WebClient pour les appels aux services externes
 */
@Configuration
public class WebClientConfiguration {

    @Value("${api.auth.url:http://localhost:8081}")
    private String urlApiAuth;

    @Value("${api.paiement.url:http://localhost:8082}")
    private String urlApiPaiement;

    /**
     * Bean WebClient pour l'API d'authentification
     */
    @Bean(name = "webClientAuth")
    public WebClient webClientAuth() {
        return WebClient.builder()
                .baseUrl(urlApiAuth)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Bean WebClient pour l'API de paiement
     */
    @Bean(name = "webClientPaiement")
    public WebClient webClientPaiement() {
        return WebClient.builder()
                .baseUrl(urlApiPaiement)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
