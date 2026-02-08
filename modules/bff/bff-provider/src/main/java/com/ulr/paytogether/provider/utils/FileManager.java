package com.ulr.paytogether.provider.utils;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Gestionnaire de fichiers avec MinIO
 * Utilitaire pour uploader, télécharger et récupérer des fichiers depuis MinIO
 */
@Component
public class FileManager {

    @Value("${minio.bucket.name}")
    private String bucketName;
    @Value("${minio.presigned.url.expiration}")
    private int presignedUrlExpiry; // Durée de validité des URL pré-signées en secondes (15 minutes par défaut)

    @Autowired
    private MinioClient minioClient;

    /**
     * Méthode de test
     */
    public String test() {
        return "Hello world";
    }

    /**
     * Upload un fichier vers MinIO
     * @param inputStream le flux d'entrée du fichier
     * @param uniqueFileName le nom unique du fichier
     * @param folderName le nom du dossier de destination
     * @param size la taille du fichier
     */
    public void uploadMinioFile(InputStream inputStream, String uniqueFileName, String folderName, long size) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(uniqueFileName);
        System.out.println("put object with name " + uniqueFileName + " and mime type " + mimeType + " max size " + size);

        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .contentType(mimeType)
                    .object(folderName + "/" + uniqueFileName)
                    .stream(inputStream, size, -1)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier vers MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère un fichier depuis MinIO et le retourne comme File temporaire
     * @param uniqueFileName le nom unique du fichier
     * @return le fichier temporaire
     */
    public File getMinioFile(String uniqueFileName) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs
                .builder()
                .bucket(bucketName)
                .object(uniqueFileName)
                .build())) {

            String extension = FilenameUtils.getExtension(uniqueFileName);
            File tempFile = File.createTempFile("PAYTOGETHER-" + System.currentTimeMillis(), "." + extension);
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(stream, out);
            }

            return tempFile;
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException("Erreur lors de la récupération du fichier depuis MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Télécharge un fichier depuis MinIO vers le système de fichiers local
     * @param uniqueFileName le nom unique du fichier
     */
    public void downloadMinioFile(String uniqueFileName) {
        try {
            minioClient.downloadObject(DownloadObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(uniqueFileName)
                    .filename(uniqueFileName)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException("Erreur lors du téléchargement du fichier depuis MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Génère une URL pré-signée pour uploader un fichier vers MinIO
     * @param uniqueFileName le nom unique du fichier
     * @return l'URL pré-signée
     */
    public String generatePresignedUrl(String uniqueFileName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs
                    .builder()
                    .bucket(bucketName)
                    .object(uniqueFileName)
                    .method(Method.PUT)
                    .expiry(presignedUrlExpiry)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException("Erreur lors de la génération de l'URL pré-signée pour MinIO: " + e.getMessage(), e);
        }
    }

}
