package com.ulr.paytogether.core.exception;

/**
 * Exception levée lorsqu'une ressource existe déjà (duplication)
 * Code d'erreur traduisible côté frontend
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String errorCode) {
        super(errorCode);
    }

    public DuplicateResourceException(String errorCode, Object... params) {
        super(errorCode, params);
    }

    /**
     * Constructeur pratique pour un email déjà existant
     *
     * @param email email en duplication
     */
    public static DuplicateResourceException emailExistant(String email) {
        return new DuplicateResourceException("utilisateur.email.existe", email);
    }
}

