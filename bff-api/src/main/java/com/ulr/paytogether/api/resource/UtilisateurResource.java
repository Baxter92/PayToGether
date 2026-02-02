package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.UtilisateurApiAdapter;
import com.ulr.paytogether.api.dto.CreerUtilisateurDTO;
import com.ulr.paytogether.api.dto.UtilisateurDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Resource REST pour la gestion des utilisateurs
 * Utilise l'ApiAdapter selon l'architecture hexagonale
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Slf4j
public class UtilisateurResource {

    private final UtilisateurApiAdapter apiAdapter;

    /**
     * Créer un nouvel utilisateur
     */
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
            @Valid @RequestBody UtilisateurDTO dto) {
        log.info("Mise à jour de l'utilisateur: {}", uuid);

        try {
            UtilisateurDTO mis_a_jour = apiAdapter.mettreAJour(uuid, dto);
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
}
