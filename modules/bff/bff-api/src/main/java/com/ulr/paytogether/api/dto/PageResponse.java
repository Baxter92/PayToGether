package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO générique pour les réponses paginées
 * @param <T> Type des éléments de la page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * Contenu de la page
     */
    private List<T> content;

    /**
     * Numéro de la page actuelle (commence à 0)
     */
    private int page;

    /**
     * Taille de la page (nombre d'éléments par page)
     */
    private int size;

    /**
     * Nombre total d'éléments
     */
    private long totalElements;

    /**
     * Nombre total de pages
     */
    private int totalPages;

    /**
     * Est-ce la première page ?
     */
    private boolean first;

    /**
     * Est-ce la dernière page ?
     */
    private boolean last;

    /**
     * Y a-t-il une page suivante ?
     */
    private boolean hasNext;

    /**
     * Y a-t-il une page précédente ?
     */
    private boolean hasPrevious;
}

