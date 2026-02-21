package com.ulr.paytogether.wsclient.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client REST pour gérer des listes d'objets génériques
 *
 * Utile pour les endpoints qui retournent des listes d'éléments
 *
 * @param <R> Type de la requête
 * @param <T> Type des éléments de la liste en réponse
 */
public abstract class AbstractListRestClient<R, T> extends AbstractRestClient<R, List<T>> {

    public AbstractListRestClient(RestClient restClient) {
        super(restClient);
    }

    /**
     * Cette méthode doit être implémentée par les classes enfants
     * pour fournir le bon TypeReference pour List<T>
     */
    @Override
    protected abstract ParameterizedTypeReference<List<T>> getTypeReference();
}
