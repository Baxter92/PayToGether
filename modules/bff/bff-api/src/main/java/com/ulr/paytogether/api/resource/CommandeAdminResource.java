package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.CommandeAdminApiAdapter;
import com.ulr.paytogether.api.dto.CommandeListResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource REST pour l'administration des commandes
 * Endpoints protégés pour le rôle ADMIN uniquement
 */
@RestController
@RequestMapping("/api/admin/commandes")
@RequiredArgsConstructor
@Slf4j
public class CommandeAdminResource {

    private final CommandeAdminApiAdapter commandeAdminApiAdapter;

    /**
     * Liste toutes les commandes avec statistiques globales
     * Accessible uniquement aux ADMIN
     *
     * @return Liste des commandes et statistiques
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommandeListResponseDTO> listerToutesLesCommandes() {
        log.info("Admin : Récupération de la liste complète des commandes");

        CommandeListResponseDTO response = commandeAdminApiAdapter.listerToutesLesCommandes();

        log.info("Admin : {} commandes trouvées", response.getCommandes().size());
        return ResponseEntity.ok(response);
    }
}

