package com.ulr.paytogether.core.exception;

import java.util.UUID;

/**
 * Exception levée lorsqu'une ressource n'est pas trouvée
 * Code d'erreur traduisible côté frontend
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(String errorCode, Object... params) {
        super(errorCode, params);
    }

    /**
     * Constructeur pratique pour une ressource non trouvée par UUID
     *
     * @param resourceType type de ressource (ex: "deal", "utilisateur")
     * @param uuid UUID de la ressource
     */
    public static ResourceNotFoundException parUuid(String resourceType, UUID uuid) {
        return new ResourceNotFoundException(resourceType + ".non.trouve", uuid.toString());
    }
}

