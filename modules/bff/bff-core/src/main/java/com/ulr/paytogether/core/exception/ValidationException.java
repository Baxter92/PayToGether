package com.ulr.paytogether.core.exception;

/**
 * Exception levée lorsqu'une validation métier échoue
 * Code d'erreur traduisible côté frontend
 */
public class ValidationException extends BusinessException {

    public ValidationException(String errorCode) {
        super(errorCode);
    }

    public ValidationException(String errorCode, Object... params) {
        super(errorCode, params);
    }

    public ValidationException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ValidationException(String errorCode, Throwable cause, Object... params) {
        super(errorCode, cause, params);
    }
}

