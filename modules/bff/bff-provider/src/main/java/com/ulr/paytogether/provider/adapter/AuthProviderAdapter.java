package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.LoginModele;
import com.ulr.paytogether.core.modele.LoginResponseModele;
import com.ulr.paytogether.core.modele.MeResponseModele;
import com.ulr.paytogether.core.provider.AuthProvider;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import com.ulr.paytogether.wsclient.client.apiclient.AuthApiCLient;
import com.ulr.paytogether.wsclient.client.apiclient.UserApiClient;
import com.ulr.paytogether.wsclient.dto.LoginResponse;
import com.ulr.paytogether.wsclient.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Adaptateur pour l'authentification
 * Utilise les clients Keycloak pour les opérations d'authentification
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthProviderAdapter implements AuthProvider {

    private final AuthApiCLient authApiClient;
    private final UserApiClient userApiClient;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public LoginResponseModele login(LoginModele login) {
        log.info("Appel à Keycloak pour authentifier l'utilisateur: {}", login.getUsername());
        if (utilisateurRepository.existsByEmail(login.getUsername())) {
            log.warn("Tentative d'authentification pour un utilisateur qui n'existe pas: {}", login.getUsername());
            throw new RuntimeException("Utilisateur non trouvé. Vérifiez vos identifiants.");
        }

        try {
            LoginResponse response = authApiClient.getToken(login.getUsername(), login.getPassword());

            return LoginResponseModele.builder()
                    .accessToken(response.getAccessToken())
                    .refreshToken(response.getRefreshToken())
                    .tokenType(response.getTokenType())
                    .expiresIn(response.getExpiresIn())
                    .refreshExpiresIn(response.getRefreshExpiresIn())
                    .scope(response.getScope())
                    .build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de l'authentification: {}", e.getMessage());
            throw new RuntimeException("Échec de l'authentification. Vérifiez vos identifiants.", e);
        }
    }

    @Override
    public MeResponseModele getMe(String token) {
        log.debug("Récupération des informations utilisateur depuis Keycloak");

        try {
            // Extraire l'ID utilisateur du JWT
            String userId = extractUserIdFromToken(token);
            if (utilisateurRepository.existsByKeycloakId(userId)) {
                log.warn("Tentative d'authentification pour un utilisateur id qui n'existe pas: {}", userId);
                throw new RuntimeException("Utilisateur non trouvé. Vérifiez vos identifiants.");
            }
            // Récupérer les détails de l'utilisateur depuis Keycloak
            UserResponse userResponse = userApiClient.getUser(token, userId);

            return MeResponseModele.builder()
                    .id(userResponse.getId())
                    .username(userResponse.getUsername())
                    .email(userResponse.getEmail())
                    .prenom(userResponse.getFirstName())
                    .nom(userResponse.getLastName())
                    .actif(userResponse.isEnabled())
                    .emailVerifie(userResponse.isEmailVerified())
                    .dateCreationTimestamp(userResponse.getCreatedTimestamp())
                    .roles(userResponse.getRoles())
                    .build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération des informations utilisateur: {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer les informations de l'utilisateur", e);
        }
    }

    @Override
    public void logout(String token) {
        log.info("Déconnexion de l'utilisateur");

        try {
            String userId = extractUserIdFromToken(token);
            if (utilisateurRepository.existsByKeycloakId(userId)) {
                log.warn("Tentative de déconnexion pour un utilisateur id qui n'existe pas: {}", userId);
                throw new RuntimeException("Utilisateur non trouvé. Vérifiez vos identifiants.");
            }
            authApiClient.logout(token);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            throw new RuntimeException("Échec de la déconnexion", e);
        }
    }

    /**
     * Extrait l'ID utilisateur du JWT
     * @param token token JWT
     * @return ID utilisateur (sub claim)
     */
    private String extractUserIdFromToken(String token) {
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
            log.error("Erreur lors de l'extraction de l'ID utilisateur: {}", e.getMessage());
            throw new RuntimeException("Token invalide", e);
        }
    }
}

