package com.ulr.paytogether.api.exception;

import com.ulr.paytogether.api.dto.ErrorResponseDTO;
import com.ulr.paytogether.core.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global des exceptions
 * Intercepte toutes les exceptions et les transforme en réponses HTTP appropriées
 * avec des codes d'erreur traduisibles côté frontend
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestion des erreurs de validation métier
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(ValidationException ex) {
        log.warn("Erreur de validation: {} avec paramètres: {}", ex.getErrorCode(), ex.getParams());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getErrorCode(),
                ex.getParams(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gestion des ressources non trouvées
     * HTTP 404 - Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Ressource non trouvée: {} avec paramètres: {}", ex.getErrorCode(), ex.getParams());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getErrorCode(),
                ex.getParams(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Gestion des duplications de ressources
     * HTTP 409 - Conflict
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResourceException(DuplicateResourceException ex) {
        log.warn("Ressource en duplication: {} avec paramètres: {}", ex.getErrorCode(), ex.getParams());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getErrorCode(),
                ex.getParams(),
                HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Gestion des opérations interdites
     * HTTP 403 - Forbidden
     */
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbiddenOperationException(ForbiddenOperationException ex) {
        log.warn("Opération interdite: {} avec paramètres: {}", ex.getErrorCode(), ex.getParams());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getErrorCode(),
                ex.getParams(),
                HttpStatus.FORBIDDEN.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Gestion des erreurs de stockage de fichiers (MinIO)
     * HTTP 500 - Internal Server Error
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileStorageException(FileStorageException ex) {
        log.error("Erreur de stockage de fichier: {} avec paramètres: {}", ex.getErrorCode(), ex.getParams(), ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getErrorCode(),
                ex.getParams(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Gestion des erreurs de validation Jakarta (annotations @NotNull, @NotBlank, etc.)
     * HTTP 400 - Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Erreur de validation Jakarta: {}", ex.getMessage());

        // Récupérer le premier message d'erreur
        String errorCode = "validation.champ.invalide";
        if (ex.getBindingResult().hasFieldErrors()) {
            String field = ex.getBindingResult().getFieldErrors().get(0).getField();
            errorCode = "validation." + field + ".invalide";
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
                errorCode,
                new Object[0],
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gestion de toutes les autres exceptions non prévues
     * HTTP 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Erreur non gérée: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                "erreur.interne",
                new Object[0],
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

