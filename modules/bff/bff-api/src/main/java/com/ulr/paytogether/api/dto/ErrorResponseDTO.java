package com.ulr.paytogether.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les réponses d'erreur
 * Contient un code d'erreur traduisible côté frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    /**
     * Code d'erreur traduisible
     * Format: {entité}.{attribut}.{type}
     * Exemple: "deal.titre.obligatoire"
     */
    private String errorCode;

    /**
     * Paramètres pour la traduction
     * Exemple: ["5000"] pour "La description ne peut pas dépasser {0} caractères"
     */
    private Object[] params;

    /**
     * Code HTTP
     * Exemple: 400, 404, 409, etc.
     */
    private int status;

    /**
     * Timestamp de l'erreur
     */
    private long timestamp;

    public ErrorResponseDTO(String errorCode, Object[] params, int status) {
        this.errorCode = errorCode;
        this.params = params;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
}

