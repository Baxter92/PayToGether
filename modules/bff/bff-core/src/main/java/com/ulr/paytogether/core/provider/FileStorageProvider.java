package com.ulr.paytogether.core.provider;

import java.io.InputStream;

/**
 * Provider pour le stockage de fichiers (MinIO)
 */
public interface FileStorageProvider {
    
    /**
     * Upload un fichier vers le stockage
     */
    void uploadFile(InputStream inputStream, String fileName, String directory, long size);
    
    /**
     * Génère une URL présignée pour lecture
     */
    String generateReadUrl(String filePath);
}

