package com.ulr.paytogether.core.modele;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Modèle générique pour les résultats paginés
 * @param <T> Type des éléments de la page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageModele<T> {

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
}

