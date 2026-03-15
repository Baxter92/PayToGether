package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.AuthApiAdapter;
import com.ulr.paytogether.api.apiadapter.UtilisateurApiAdapter;
import com.ulr.paytogether.api.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * Resource REST pour l'authentification
 * Utilise l'ApiAdapter selon l'architecture hexagonale
 * Validation gérée par les Validators du core (pas d'annotations @Valid)
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthResource {

    private final AuthApiAdapter apiAdapter;
    private final UtilisateurApiAdapter utilisateurApiAdapter;

    /**
     * Authentifier un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        log.info("Requête de connexion pour l'utilisateur: {}", loginDTO.getUsername());

        try {
            LoginResponseDTO response = apiAdapter.login(loginDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la connexion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Inscription d'un utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<UtilisateurDTO> register( @RequestBody CreerUtilisateurDTO dto) {
        log.info("Requête de création de compte ");

        UtilisateurDTO cree = utilisateurApiAdapter.creer(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cree);
    }


    /**
     * Obtenir les informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> getMe(@Nullable JwtAuthenticationToken token) {
        log.debug("Récupération des informations de l'utilisateur connecté");

        try {
            // ✅ Gestion du cas où le token est null (mode test sans sécurité)
            String tokenValue = token != null ? token.getToken().getTokenValue() : "test-token";
            MeResponseDTO response = apiAdapter.getMe(tokenValue);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération des informations utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Demander la réinitialisation du mot de passe (étape 1)
     * Envoie un email avec un lien de réinitialisation contenant un token
     * Route publique accessible sans authentification
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody DemanderReinitialisationMotDePasseDTO dto) {
        log.info("Requête de demande de réinitialisation de mot de passe pour: {}", dto.getEmail());

        try {
            utilisateurApiAdapter.demanderReinitialisationMotDePasse(dto.getEmail());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la demande de réinitialisation: {}", e.getMessage());
            // Pour des raisons de sécurité, on retourne toujours 200 même si l'email n'existe pas
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la demande de réinitialisation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Réinitialiser le mot de passe avec un token (étape 2)
     * Route publique accessible sans authentification
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ReinitialiserMotDePasseDTO dto) {
        log.info("Requête de réinitialisation de mot de passe");

        try {
            utilisateurApiAdapter.reinitialiserMotDePasseAvecToken(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la réinitialisation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la réinitialisation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activer un compte utilisateur avec un token
     * Route publique accessible sans authentification
     */
    @GetMapping("/activate-account")
    public ResponseEntity<Void> activateAccount(@RequestParam("token") String token) {
        log.info("Requête d'activation de compte avec token");

        try {
            utilisateurApiAdapter.activerCompteAvecToken(token);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de l'activation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'activation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Déconnecter un utilisateur
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Nullable JwtAuthenticationToken token) {
        log.info("Requête de déconnexion");

        try {
            // ✅ Gestion du cas où le token est null (mode test sans sécurité)
            String tokenValue = token != null ? token.getToken().getTokenValue() : "test-token";
            apiAdapter.logout(tokenValue);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

