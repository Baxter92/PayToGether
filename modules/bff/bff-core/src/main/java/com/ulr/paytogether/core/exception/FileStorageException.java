package com.ulr.paytogether.core.exception;

/**
 * Exception levée lors d'erreurs liées au stockage de fichiers (MinIO)
 * Code d'erreur traduisible côté frontend
 */
public class FileStorageException extends BusinessException {

    public FileStorageException(String errorCode) {
        super(errorCode);
    }

    public FileStorageException(String errorCode, Object... params) {
        super(errorCode, params);
    }

    public FileStorageException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public FileStorageException(String errorCode, Throwable cause, Object... params) {
        super(errorCode, cause, params);
    }
}

