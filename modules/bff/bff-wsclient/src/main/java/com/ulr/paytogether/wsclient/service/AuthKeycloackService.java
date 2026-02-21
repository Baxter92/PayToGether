package com.ulr.paytogether.wsclient.service;

import com.ulr.paytogether.wsclient.client.AbstractRestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuthKeycloackService<T,R> extends AbstractRestClient<T, R> {


    public AuthKeycloackService(RestClient restClient) {
        super(restClient);
    }

    /**
     * Implémentation de la méthode abstraite pour fournir le TypeReference
     *
     * @return Le TypeReference pour LoginResponse
     */
    @Override
    protected ParameterizedTypeReference<R> getTypeReference() {
        return new  ParameterizedTypeReference<>() {};
    }

    /**
     * Gestion personnalisée des erreurs pour l'authentification
     *
     * @param exception L'exception à gérer
     */
    @Override
    protected void handleError(Exception exception) {
        // Appeler la gestion par défaut
        super.handleError(exception);

    }
}
