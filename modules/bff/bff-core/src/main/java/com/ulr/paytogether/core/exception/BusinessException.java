package com.ulr.paytogether.core.exception;

import lombok.Getter;

/**
 * Exception métier de base pour PayToGether
 * Contient un code d'erreur traduisible côté frontend
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] params;

    /**
     * Constructeur avec code d'erreur uniquement
     *
     * @param errorCode code d'erreur (ex: "deal.titre.obligatoire")
     */
    public BusinessException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    /**
     * Constructeur avec code d'erreur et paramètres
     *
     * @param errorCode code d'erreur (ex: "deal.description.longueur")
     * @param params paramètres pour la traduction (ex: [5000])
     */
    public BusinessException(String errorCode, Object... params) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = params;
    }

    /**
     * Constructeur avec code d'erreur et cause
     *
     * @param errorCode code d'erreur
     * @param cause exception d'origine
     */
    public BusinessException(String errorCode, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    /**
     * Constructeur complet avec code d'erreur, paramètres et cause
     *
     * @param errorCode code d'erreur
     * @param cause exception d'origine
     * @param params paramètres pour la traduction
     */
    public BusinessException(String errorCode, Throwable cause, Object... params) {
        super(errorCode, cause);
        this.errorCode = errorCode;
        this.params = params;
    }
}

