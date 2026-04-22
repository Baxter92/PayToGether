package com.ulr.paytogether.configuration;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration pour RestClient avec gestion des timeouts.
 * Les timeouts sont configurables via application.properties.
 */
@Configuration
public class RestClientConfig {

    /** Délai max pour établir la connexion TCP (ms) */
    @Value("${restclient.timeout.connect:10000}")
    private int connectTimeoutMs;

    /** Délai max pour lire la réponse (ms) — inclut le traitement côté serveur externe */
    @Value("${restclient.timeout.read:30000}")
    private int readTimeoutMs;

    /** Délai max pour obtenir une connexion depuis le pool (ms) */
    @Value("${restclient.timeout.connection-request:10000}")
    private int connectionRequestTimeoutMs;

    @Bean
    public RestClient restClient() {
        // Configuration du pool de connexions avec timeout de connexion
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                        .setSocketTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                        .build())
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(20)
                .build();

        // Configuration des timeouts de requête
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeoutMs))
                .setResponseTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
