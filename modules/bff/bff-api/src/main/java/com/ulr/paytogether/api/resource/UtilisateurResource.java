package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.UtilisateurApiAdapter;
import com.ulr.paytogether.api.dto.*;
import com.ulr.paytogether.core.enumeration.StatutImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Resource REST pour la gestion des utilisateurs
 * Utilise l'ApiAdapter selon l'architecture hexagonale
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Slf4j
public class UtilisateurResource {

    private final UtilisateurApiAdapter apiAdapter;

    /**
     * Créer un nouvel utilisateur
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UtilisateurDTO> creer(@Valid @RequestBody CreerUtilisateurDTO dto) {
        log.info("Création d'un utilisateur avec l'email: {}", dto.getEmail());

        UtilisateurDTO cree = apiAdapter.creer(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cree);
    }

    /**
     * Récupérer un utilisateur par son UUID
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<UtilisateurDTO> lireParUuid(@PathVariable UUID uuid) {
        log.debug("Récupération de l'utilisateur: {}", uuid);

        return apiAdapter.trouverParUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer un utilisateur par son email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> lireParEmail(@PathVariable String email) {
        log.debug("Récupération de l'utilisateur avec l'email: {}", email);

        return apiAdapter.trouverParEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer tous les utilisateurs
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> lireTous() {
        log.debug("Récupération de tous les utilisateurs");

        List<UtilisateurDTO> utilisateurs = apiAdapter.trouverTous();

        return ResponseEntity.ok(utilisateurs);
    }

    /**
     * Mettre à jour un utilisateur
     */
    @PutMapping("/{uuid}")
    public ResponseEntity<UtilisateurDTO> mettreAJour(
            @PathVariable UUID uuid,
            @Valid @RequestBody UtilisateurDTO dto, JwtAuthenticationToken token) {
        log.info("Mise à jour de l'utilisateur: {}", uuid);

        try {
            var tokenValue = token.getToken().getTokenValue();
            UtilisateurDTO mis_a_jour = apiAdapter.mettreAJour(uuid, dto, tokenValue);
            return ResponseEntity.ok(mis_a_jour);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprimer un utilisateur
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID uuid) {
        log.info("Suppression de l'utilisateur: {}", uuid);

        apiAdapter.supprimer(uuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Vérifier si un email existe
     */
    @GetMapping("/existe/{email}")
    public ResponseEntity<Boolean> existeParEmail(@PathVariable String email) {
        log.debug("Vérification de l'existence de l'email: {}", email);

        boolean existe = apiAdapter.existeParEmail(email);
        return ResponseEntity.ok(existe);
    }

    /**
     * Confirmer l'upload de la photo de profil
     * Endpoint appelé par le frontend après upload réussi vers MinIO
     */
    @PatchMapping("/{utilisateurUuid}/photo-profil/confirm")
    public ResponseEntity<Void> confirmerUploadPhotoProfil(@PathVariable UUID utilisateurUuid) {
        log.info("Confirmation upload photo de profil pour utilisateur {}", utilisateurUuid);

        try {
            apiAdapter.mettreAJourStatutPhotoProfil(
                utilisateurUuid,
                StatutImage.UPLOADED
            );
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la confirmation de l'upload: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtenir l'URL de lecture de la photo de profil
     * Génère une URL présignée pour lire la photo depuis MinIO
     */
    @GetMapping("/{utilisateurUuid}/photo-profil/url")
    public ResponseEntity<java.util.Map<String, String>> obtenirUrlPhotoProfil(@PathVariable UUID utilisateurUuid) {
        log.debug("Récupération de l'URL de lecture pour la photo de profil de l'utilisateur {}", utilisateurUuid);

        try {
            String urlLecture = apiAdapter.obtenirUrlLecturePhotoProfil(utilisateurUuid);
            return ResponseEntity.ok(java.util.Map.of("url", urlLecture));
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la récupération de l'URL de lecture: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    @PatchMapping("/{uuid}/reset-password")
    public ResponseEntity<Void> reinitialiserMotDePasse(
            @PathVariable UUID uuid,
            @Valid @RequestBody ReinitialiserMotDePasseDTO dto,
            JwtAuthenticationToken token) {
        log.info("Réinitialisation du mot de passe pour l'utilisateur: {}", uuid);

        try {
            var tokenValue = token.getToken().getTokenValue();
            apiAdapter.reinitialiserMotDePasse(uuid, dto.getNouveauMotDePasse(), tokenValue);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de la réinitialisation du mot de passe: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{uuid}/enable")
    public ResponseEntity<Void> activerUtilisateur(
            @PathVariable UUID uuid,
            @Valid @RequestBody ActiverUtilisateurDTO dto,
            JwtAuthenticationToken token) {
        log.info("Activation/Désactivation de l'utilisateur: {} - actif: {}", uuid, dto.getActif());

        try {
            var tokenValue = token.getToken().getTokenValue();
            apiAdapter.activerUtilisateur(uuid, dto.getActif(), tokenValue);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de l'activation/désactivation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Assigner un rôle à un utilisateur
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{uuid}/assign-role")
    public ResponseEntity<Void> assignerRole(
            @PathVariable UUID uuid,
            @Valid @RequestBody AssignerRoleDTO dto,
            JwtAuthenticationToken token) {
        log.info("Assignation du rôle {} à l'utilisateur: {}", dto.getNomRole(), uuid);

        try {
            var tokenValue = token.getToken().getTokenValue();
            apiAdapter.assignerRole(uuid, dto.getNomRole(), tokenValue);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur lors de l'assignation du rôle: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
