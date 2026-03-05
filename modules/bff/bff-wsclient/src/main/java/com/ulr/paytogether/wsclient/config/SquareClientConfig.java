package com.ulr.paytogether.wsclient.config;

import com.squareup.square.SquareClient;
import com.squareup.square.core.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client Square Payment API.
 * Compatible avec Square Java SDK version 46.x
 */
@Configuration
@Slf4j
public class SquareClientConfig {

    @Value("${square.access-token}")
    private String accessToken;

    @Value("${square.environment:SANDBOX}")
    private String environment;

    @Value("${square.location-id}")
    private String locationId;

    /**
     * Crée le bean SquareClient pour les appels à l'API Square
     */
    @Bean
    public SquareClient squareClient() {
        log.info("Initializing Square Client with environment: {}", environment);

        Environment env = "PRODUCTION".equalsIgnoreCase(environment)
            ? Environment.PRODUCTION
            : Environment.SANDBOX;

        SquareClient client = SquareClient.builder()
            .environment(env)
                .token(accessToken)
            .build();

        log.info("Square Client initialized successfully");
        return client;
    }

    /**
     * Retourne l'ID de location Square configuré
     */
    @Bean
    public String squareLocationId() {
        return locationId;
    }
}

