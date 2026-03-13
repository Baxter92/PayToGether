package com.ulr.paytogether.core.domaine.service;

import java.io.InputStream;

/**
 * Service pour gérer les fichiers (MinIO)
 */
public interface FileStorageService {
    
    /**
     * Upload un fichier vers le stockage
     * @param inputStream Flux du fichier
     * @param fileName Nom unique du fichier
     * @param directory Répertoire de destination
     * @param size Taille du fichier
     */
    void uploadFile(InputStream inputStream, String fileName, String directory, long size);
    
    /**
     * Génère une URL présignée pour lecture d'un fichier
     * @param filePath Chemin complet du fichier
     * @return URL présignée pour téléchargement
     */
    String generateReadUrl(String filePath);
}

