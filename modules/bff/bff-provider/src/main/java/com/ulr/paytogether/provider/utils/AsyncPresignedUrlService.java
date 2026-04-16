package com.ulr.paytogether.provider.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour la génération asynchrone et parallèle des URLs présignées MinIO
 * Utilise les Virtual Threads pour optimiser les appels I/O
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncPresignedUrlService {

    private final FileManager fileManager;

    /**
     * Génère une URL présignée de manière asynchrone
     *
     * @param directory Répertoire MinIO
     * @param fileName Nom du fichier
     * @return CompletableFuture contenant l'URL présignée
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<String> generatePresignedUrlAsync(String directory, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fileManager.generatePresignedUrl(directory, fileName);
            } catch (Exception e) {
                log.error("Erreur génération URL présignée pour {}/{}: {}", directory, fileName, e.getMessage());
                return null;
            }
        });
    }

    /**
     * Génère une URL présignée en lecture de manière asynchrone
     *
     * @param fullFileName Chemin complet du fichier
     * @return CompletableFuture contenant l'URL présignée
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<String> generatePresignedUrlForReadAsync(String fullFileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fileManager.generatePresignedUrlForRead(fullFileName);
            } catch (Exception e) {
                log.error("Erreur génération URL lecture pour {}: {}", fullFileName, e.getMessage());
                return null;
            }
        });
    }

    /**
     * Génère plusieurs URLs présignées en parallèle
     *
     * @param directory Répertoire MinIO
     * @param fileNames Liste des noms de fichiers
     * @return Liste des URLs présignées (dans le même ordre)
     */
    public List<String> generatePresignedUrlsInParallel(String directory, List<String> fileNames) {
        long start = System.currentTimeMillis();

        // Génération en parallèle avec Virtual Threads
        List<CompletableFuture<String>> futures = fileNames.stream()
                .map(fileName -> generatePresignedUrlAsync(directory, fileName))
                .toList();

        // Attendre que toutes les URLs soient générées
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // Récupérer les résultats
        List<String> urls = allFutures.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .toList()
        ).join();

        long duration = System.currentTimeMillis() - start;
        log.debug("✅ {} URLs présignées générées en {}ms (parallèle)", fileNames.size(), duration);

        return urls;
    }
}

