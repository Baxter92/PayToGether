package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.PaiementAdminApiAdapter;
import com.ulr.paytogether.api.dto.PaiementListResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource REST pour l'administration des paiements
 * Endpoints protégés pour le rôle ADMIN uniquement
 */
@RestController
@RequestMapping("/api/admin/paiements")
@RequiredArgsConstructor
@Slf4j
public class PaiementAdminResource {

    private final PaiementAdminApiAdapter paiementAdminApiAdapter;

    /**
     * Liste tous les paiements avec statistiques globales
     * Accessible uniquement aux ADMIN
     *
     * @return Liste des paiements et statistiques
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementListResponseDTO> listerTousLesPaiements() {
        log.info("Admin : Récupération de la liste complète des paiements");

        PaiementListResponseDTO response = paiementAdminApiAdapter.listerTousLesPaiements();

        log.info("Admin : {} paiements trouvés", response.getPaiements().size());
        return ResponseEntity.ok(response);
    }
}

