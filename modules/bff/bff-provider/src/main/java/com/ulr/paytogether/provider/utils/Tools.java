package com.ulr.paytogether.provider.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Base64;

@UtilityClass
public class Tools {

    public static final String DIRECTORY_DEALS_IMAGES = "deals/";
    public static final String DIRECTORY_PUBLICITES_IMAGES = "publicites/";
    public static final String DIRECTORY_UTILISATEUR_IMAGES = "utilisateurs/";

    /**
     * Extrait l'ID utilisateur du JWT
     * @param token token JWT
     * @return ID utilisateur (sub claim)
     */
    public static String extractUserIdFromToken(String token) {
        try {
            // Décoder le JWT (format: header.payload.signature)
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Token JWT invalide");
            }

            // Décoder la partie payload (base64)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Parser le JSON pour extraire le "sub" (subject/user ID)
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload);
            String userId = jsonNode.get("sub").asText();

            if (userId == null || userId.isEmpty()) {
                throw new IllegalStateException("Le token ne contient pas de subject (sub)");
            }

            return userId;
        } catch (Exception e) {
            throw new RuntimeException("Token invalide", e);
        }
    }
}
