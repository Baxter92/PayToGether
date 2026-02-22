package com.ulr.paytogether.wsclient.service;

import com.ulr.paytogether.wsclient.client.AbstractRestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserKeycloackService<T,R> extends AbstractRestClient<T, R> {
    public UserKeycloackService(RestClient restClient) {
        super(restClient);
    }

    @Override
    protected ParameterizedTypeReference<R> getTypeReference() {
        return new  ParameterizedTypeReference<>() {};
    }

    @Override
    protected void handleError(Exception exception) {
        // Appeler la gestion par défaut
        super.handleError(exception);
    }
}
