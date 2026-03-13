package com.ulr.paytogether.api.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Contrôleur pour servir les ressources statiques (logos, images publiques)
 * Ces ressources sont accessibles sans authentification
 */
@Slf4j
@RestController
@RequestMapping("/images")
public class StaticResourceController {

    /**
     * Servir le logo de l'application
     * Accessible publiquement pour l'affichage dans les emails
     *
     * @return Image du logo
     */
    @GetMapping("/dealtogetherlogo.png")
    public ResponseEntity<Resource> getLogo() {
        try {
            Resource resource = new ClassPathResource("dealtogetherlogo.png");
            
            if (!resource.exists()) {
                log.error("❌ Logo introuvable : dealtogetherlogo.png");
                return ResponseEntity.notFound().build();
            }
            
            log.debug("✅ Serving logo: dealtogetherlogo.png");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache 24h
                    .contentType(MediaType.IMAGE_PNG)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("❌ Erreur lors de la lecture du logo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint générique pour servir d'autres images statiques si nécessaire
     *
     * @param filename Nom du fichier image
     * @return Image demandée
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getStaticImage(@PathVariable String filename) {
        try {
            // Validation du nom de fichier (sécurité)
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                log.warn("⚠️ Tentative d'accès à un fichier non autorisé : {}", filename);
                return ResponseEntity.badRequest().build();
            }
            
            Resource resource = new ClassPathResource(filename);
            
            if (!resource.exists()) {
                log.warn("⚠️ Ressource introuvable : {}", filename);
                return ResponseEntity.notFound().build();
            }
            
            // Déterminer le type MIME
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (filename.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (filename.endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            } else if (filename.endsWith(".svg")) {
                mediaType = MediaType.valueOf("image/svg+xml");
            }
            
            log.debug("✅ Serving static resource: {}", filename);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache 24h
                    .contentType(mediaType)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("❌ Erreur lors de la lecture de la ressource : {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

