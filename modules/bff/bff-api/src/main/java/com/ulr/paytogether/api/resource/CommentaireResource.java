package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.CommentaireApiAdapter;
import com.ulr.paytogether.api.dto.CommentaireDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des commentaires
 * Endpoints : /api/commentaires
 */
@RestController
@RequestMapping("/api/commentaires")
@RequiredArgsConstructor
@Slf4j
public class CommentaireResource {

    private final CommentaireApiAdapter apiAdapter;

    /**
     * Créer un nouveau commentaire
     * POST /api/commentaires
     */
    @PostMapping
    public ResponseEntity<CommentaireDTO> creer(@Valid @RequestBody CommentaireDTO dto) {
        log.info("Requête de création d'un commentaire pour le deal: {}", dto.getDealUuid());

        CommentaireDTO cree = apiAdapter.creer(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cree);
    }

    /**
     * Récupérer un commentaire par son UUID
     * GET /api/commentaires/{uuid}
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<CommentaireDTO> trouverParUuid(@PathVariable UUID uuid) {
        log.info("Requête de lecture du commentaire: {}", uuid);

        return apiAdapter.trouverParUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupérer tous les commentaires d'un deal
     * GET /api/commentaires/deal/{dealUuid}
     */
    @GetMapping("/deal/{dealUuid}")
    public ResponseEntity<List<CommentaireDTO>> trouverParDeal(@PathVariable UUID dealUuid) {
        log.info("Requête de lecture des commentaires du deal: {}", dealUuid);

        List<CommentaireDTO> commentaires = apiAdapter.trouverParDeal(dealUuid);

        return ResponseEntity.ok(commentaires);
    }

    /**
     * Mettre à jour un commentaire
     * PUT /api/commentaires/{uuid}
     */
    @PutMapping("/{uuid}")
    public ResponseEntity<CommentaireDTO> mettreAJour(
            @PathVariable UUID uuid,
            @Valid @RequestBody CommentaireDTO dto) {
        log.info("Requête de mise à jour du commentaire: {}", uuid);

        CommentaireDTO mis_a_jour = apiAdapter.mettreAJour(uuid, dto);

        return ResponseEntity.ok(mis_a_jour);
    }

    /**
     * Supprimer un commentaire
     * DELETE /api/commentaires/{uuid}
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID uuid) {
        log.info("Requête de suppression du commentaire: {}", uuid);

        apiAdapter.supprimer(uuid);

        return ResponseEntity.noContent().build();
    }
}

