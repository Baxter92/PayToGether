package com.ulr.paytogether.wsclient.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe abstraite générique pour les appels REST
 *
 * @param <R> Type de la requête (Request)
 * @param <T> Type de la réponse (Response)
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRestClient<R, T> {

    protected final RestClient restClient;

    /**
     * Retourne le TypeReference pour la désérialisation de la réponse
     * Cette méthode doit être implémentée par les classes enfants
     *
     * @return ParameterizedTypeReference pour le type T
     */
    protected abstract ParameterizedTypeReference<T> getTypeReference();

    /**
     * Effectue une requête POST
     *
     * @param url URL de destination
     * @param request Corps de la requête
     * @return La réponse désérialisée en type T
     */
    public T post(String url, R request) {
        return post(url, request, new HashMap<>());
    }

    /**
     * Effectue une requête POST avec des headers personnalisés
     *
     * @param url URL de destination
     * @param request Corps de la requête
     * @param headers Headers HTTP à ajouter
     * @return La réponse désérialisée en type T
     */
    public T post(String url, R request, Map<String, String> headers) {
        try {
            log.debug("POST request to: {}", url);

            RestClient.RequestBodySpec requestSpec = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);

            // Ajouter les headers personnalisés
            headers.forEach(requestSpec::header);

            T response = requestSpec
                .body(request)
                .retrieve()
                .body(getTypeReference());

            log.debug("POST request successful to: {}", url);
            return response;

        } catch (RestClientResponseException e) {
            log.error("HTTP Error {} lors du POST vers {}: {}",
                e.getStatusCode().value(), url, e.getResponseBodyAsString());
            handleError(e);
            throw new RestClientException("Erreur HTTP lors de la requête POST", e);

        } catch (RestClientException e) {
            log.error("Erreur RestClient lors du POST vers {}: {}", url, e.getMessage());
            handleError(e);
            throw e;

        } catch (Exception e) {
            log.error("Erreur inattendue lors du POST vers {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Erreur inattendue lors de la requête POST", e);
        }
    }

    /**
     * Effectue une requête POST sans corps de requête
     *
     * @param url URL de destination
     * @return La réponse désérialisée en type T
     */
    public T post(String url) {
        return post(url, new HashMap<>());
    }

    /**
     * Effectue une requête POST sans corps de requête avec des headers
     *
     * @param url URL de destination
     * @param headers Headers HTTP à ajouter
     * @return La réponse désérialisée en type T
     */
    public T post(String url, Map<String, String> headers) {
        try {
            log.debug("POST request (no body) to: {}", url);

            RestClient.RequestHeadersSpec<?> requestSpec = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);

            // Ajouter les headers personnalisés
            headers.forEach(requestSpec::header);

            T response = requestSpec
                .retrieve()
                .body(getTypeReference());

            log.debug("POST request successful to: {}", url);
            return response;

        } catch (RestClientResponseException e) {
            log.error("HTTP Error {} lors du POST vers {}: {}",
                e.getStatusCode().value(), url, e.getResponseBodyAsString());
            handleError(e);
            throw new RestClientException("Erreur HTTP lors de la requête POST", e);

        } catch (RestClientException e) {
            log.error("Erreur RestClient lors du POST vers {}: {}", url, e.getMessage());
            handleError(e);
            throw e;

        } catch (Exception e) {
            log.error("Erreur inattendue lors du POST vers {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Erreur inattendue lors de la requête POST", e);
        }
    }

    /**
     * Effectue une requête GET
     *
     * @param url URL de destination
     * @return La réponse désérialisée en type T
     */
    public T get(String url) {
        return get(url, new HashMap<>());
    }

    /**
     * Effectue une requête GET avec des headers personnalisés
     *
     * @param url URL de destination
     * @param headers Headers HTTP à ajouter
     * @return La réponse désérialisée en type T
     */
    public T get(String url, Map<String, String> headers) {
        try {
            log.debug("GET request to: {}", url);

            RestClient.RequestHeadersSpec<?> requestSpec = restClient.get()
                .uri(url);

            // Ajouter les headers personnalisés
            headers.forEach(requestSpec::header);

            T response = requestSpec
                .retrieve()
                .body(getTypeReference());

            log.debug("GET request successful to: {}", url);
            return response;

        } catch (RestClientResponseException e) {
            log.error("HTTP Error {} lors du GET vers {}: {}",
                e.getStatusCode().value(), url, e.getResponseBodyAsString());
            handleError(e);
            throw new RestClientException("Erreur HTTP lors de la requête GET", e);

        } catch (RestClientException e) {
            log.error("Erreur RestClient lors du GET vers {}: {}", url, e.getMessage());
            handleError(e);
            throw e;

        } catch (Exception e) {
            log.error("Erreur inattendue lors du GET vers {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Erreur inattendue lors de la requête GET", e);
        }
    }

    /**
     * Effectue une requête GET avec un token d'authentification
     *
     * @param url URL de destination
     * @param bearerToken Token d'authentification
     * @return La réponse désérialisée en type T
     */
    public T getWithAuth(String url, String bearerToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        return get(url, headers);
    }

    /**
     * Effectue une requête POST avec un token d'authentification
     *
     * @param url URL de destination
     * @param request Corps de la requête
     * @param bearerToken Token d'authentification
     * @return La réponse désérialisée en type T
     */
    public T postWithAuth(String url, R request, String bearerToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        return post(url, request, headers);
    }

    /**
     * Effectue une requête PUT
     *
     * @param url URL de destination
     * @param request Corps de la requête
     * @return La réponse désérialisée en type T
     */
    public T put(String url, R request) {
        return put(url, request, new HashMap<>());
    }

    /**
     * Effectue une requête PUT avec des headers personnalisés
     *
     * @param url URL de destination
     * @param request Corps de la requête
     * @param headers Headers HTTP à ajouter
     * @return La réponse désérialisée en type T
     */
    public T put(String url, R request, Map<String, String> headers) {
        try {
            log.debug("PUT request to: {}", url);

            RestClient.RequestBodySpec requestSpec = restClient.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);

            // Ajouter les headers personnalisés
            headers.forEach(requestSpec::header);

            T response = requestSpec
                .body(request)
                .retrieve()
                .body(getTypeReference());

            log.debug("PUT request successful to: {}", url);
            return response;

        } catch (RestClientResponseException e) {
            log.error("HTTP Error {} lors du PUT vers {}: {}",
                e.getStatusCode().value(), url, e.getResponseBodyAsString());
            handleError(e);
            throw new RestClientException("Erreur HTTP lors de la requête PUT", e);

        } catch (RestClientException e) {
            log.error("Erreur RestClient lors du PUT vers {}: {}", url, e.getMessage());
            handleError(e);
            throw e;

        } catch (Exception e) {
            log.error("Erreur inattendue lors du PUT vers {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Erreur inattendue lors de la requête PUT", e);
        }
    }

    /**
     * Effectue une requête PUT avec un token d'authentification
     *
     * @param url URL de destination
     * @param request Corps de la requête
     * @param bearerToken Token d'authentification
     * @return La réponse désérialisée en type T
     */
    public T putWithAuth(String url, R request, String bearerToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        return put(url, request, headers);
    }

    /**
     * Effectue une requête DELETE
     *
     * @param url URL de destination
     * @return La réponse désérialisée en type T
     */
    public T delete(String url) {
        return delete(url, new HashMap<>());
    }

    /**
     * Effectue une requête DELETE avec des headers personnalisés
     *
     * @param url URL de destination
     * @param headers Headers HTTP à ajouter
     * @return La réponse désérialisée en type T
     */
    public T delete(String url, Map<String, String> headers) {
        try {
            log.debug("DELETE request to: {}", url);

            RestClient.RequestHeadersSpec<?> requestSpec = restClient.delete()
                .uri(url);

            // Ajouter les headers personnalisés
            headers.forEach(requestSpec::header);

            T response = requestSpec
                .retrieve()
                .body(getTypeReference());

            log.debug("DELETE request successful to: {}", url);
            return response;

        } catch (RestClientResponseException e) {
            log.error("HTTP Error {} lors du DELETE vers {}: {}",
                e.getStatusCode().value(), url, e.getResponseBodyAsString());
            handleError(e);
            throw new RestClientException("Erreur HTTP lors de la requête DELETE", e);

        } catch (RestClientException e) {
            log.error("Erreur RestClient lors du DELETE vers {}: {}", url, e.getMessage());
            handleError(e);
            throw e;

        } catch (Exception e) {
            log.error("Erreur inattendue lors du DELETE vers {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Erreur inattendue lors de la requête DELETE", e);
        }
    }

    /**
     * Effectue une requête DELETE avec un token d'authentification
     *
     * @param url URL de destination
     * @param bearerToken Token d'authentification
     * @return La réponse désérialisée en type T
     */
    public T deleteWithAuth(String url, String bearerToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        return delete(url, headers);
    }

    /**
     * Gère les erreurs HTTP
     * Cette méthode peut être surchargée par les classes enfants pour une gestion personnalisée
     *
     * @param exception L'exception à gérer
     */
    protected void handleError(Exception exception) {
        if (exception instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            String responseBody = responseException.getResponseBodyAsString();

            switch (statusCode) {
                case 400:
                    log.error("Requête invalide (400): {}", responseBody);
                    break;
                case 401:
                    log.error("Non autorisé (401): {}", responseBody);
                    break;
                case 403:
                    log.error("Accès interdit (403): {}", responseBody);
                    break;
                case 404:
                    log.error("Ressource non trouvée (404): {}", responseBody);
                    break;
                case 500:
                    log.error("Erreur serveur interne (500): {}", responseBody);
                    break;
                default:
                    log.error("Erreur HTTP {}: {}", statusCode, responseBody);
            }
        } else {
            log.error("Erreur lors de la requête: {}", exception.getMessage());
        }
    }

    /**
     * Crée un Map de headers avec un token Bearer
     *
     * @param bearerToken Le token d'authentification
     * @return Map contenant le header Authorization
     */
    protected Map<String, String> createAuthHeaders(String bearerToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        return headers;
    }
}
