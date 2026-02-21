package com.ulr.paytogether.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Utilitaire pour manipuler les JWT
 */
public class JwtHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Décode un JWT sans vérifier sa signature
     * Utile pour extraire l'issuer avant validation
     */
    public static Jwt decodeUnverified(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("Format de token JWT invalide");
            }

            // Décoder le header
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);

            // Décoder le payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

            // Extraire les timestamps
            Instant issuedAt = claims.containsKey("iat") 
                ? Instant.ofEpochSecond(((Number) claims.get("iat")).longValue()) 
                : null;
            Instant expiresAt = claims.containsKey("exp") 
                ? Instant.ofEpochSecond(((Number) claims.get("exp")).longValue()) 
                : null;

            return new Jwt(
                token,
                issuedAt,
                expiresAt,
                header,
                claims
            );
        } catch (Exception e) {
            throw new JwtException("Erreur lors du décodage du JWT: " + e.getMessage(), e);
        }
    }

    /**
     * Extrait une claim du JWT
     */
    public static Object getClaim(Jwt jwt, String claimName) {
        return jwt.getClaims().get(claimName);
    }

    /**
     * Vérifie si le JWT est expiré
     */
    public static boolean isExpired(Jwt jwt) {
        return jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now());
    }
}
