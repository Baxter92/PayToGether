package com.ulr.paytogether.core.exception;

/**
 * Exception levée lorsqu'une opération est interdite (règle métier)
 * Code d'erreur traduisible côté frontend
 */
public class ForbiddenOperationException extends BusinessException {

    public ForbiddenOperationException(String errorCode) {
        super(errorCode);
    }

    public ForbiddenOperationException(String errorCode, Object... params) {
        super(errorCode, params);
    }

    public ForbiddenOperationException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

