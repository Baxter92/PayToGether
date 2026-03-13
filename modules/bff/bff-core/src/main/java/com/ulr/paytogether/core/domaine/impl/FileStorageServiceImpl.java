package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.FileStorageService;
import com.ulr.paytogether.core.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Implémentation du service de stockage de fichiers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    
    private final FileStorageProvider fileStorageProvider;
    
    @Override
    public void uploadFile(InputStream inputStream, String fileName, String directory, long size) {
        log.info("Upload du fichier {} dans le répertoire {}", fileName, directory);
        fileStorageProvider.uploadFile(inputStream, fileName, directory, size);
    }
    
    @Override
    public String generateReadUrl(String filePath) {
        log.debug("Génération d'URL de lecture pour: {}", filePath);
        return fileStorageProvider.generateReadUrl(filePath);
    }
}

