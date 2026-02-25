package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.AuthApiAdapter;
import com.ulr.paytogether.api.apiadapter.UtilisateurApiAdapter;
import com.ulr.paytogether.api.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * Resource REST pour l'authentification
 * Utilise l'ApiAdapter selon l'architecture hexagonale
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
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
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
    public ResponseEntity<MeResponseDTO> getMe(JwtAuthenticationToken token) {
        log.debug("Récupération des informations de l'utilisateur connecté");

        try {
            String tokenValue = token.getToken().getTokenValue();
            MeResponseDTO response = apiAdapter.getMe(tokenValue);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération des informations utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Déconnecter un utilisateur
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(JwtAuthenticationToken token) {
        log.info("Requête de déconnexion");

        try {
            String tokenValue = token.getToken().getTokenValue();
            apiAdapter.logout(tokenValue);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

