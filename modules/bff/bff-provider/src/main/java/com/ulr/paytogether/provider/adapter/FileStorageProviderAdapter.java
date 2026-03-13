package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.provider.FileStorageProvider;
import com.ulr.paytogether.provider.utils.FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Implémentation du provider de stockage de fichiers
 * Utilise FileManager pour interagir avec MinIO
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileStorageProviderAdapter implements FileStorageProvider {
    
    private final FileManager fileManager;
    
    @Override
    public void uploadFile(InputStream inputStream, String fileName, String directory, long size) {
        log.info("Provider - Upload du fichier {} dans {}", fileName, directory);
        fileManager.uploadMinioFile(inputStream, fileName, directory, size);
    }
    
    @Override
    public String generateReadUrl(String filePath) {
        log.debug("Provider - Génération d'URL de lecture pour: {}", filePath);
        return fileManager.generatePresignedUrlForRead(filePath);
    }
}

