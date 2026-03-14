package com.ulr.paytogether.api.resource;

import com.ulr.paytogether.api.apiadapter.CommandeAdminApiAdapter;
import com.ulr.paytogether.api.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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

    /**
     * Liste les commandes d'un marchand spécifique
     * Accessible aux ADMIN et VENDEUR (le vendeur ne peut voir que ses propres commandes)
     *
     * @param marchandUuid UUID du marchand
     * @return Liste des commandes du marchand et statistiques
     */
    @GetMapping("/marchand/{marchandUuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<CommandeListResponseDTO> listerCommandesMarchand(@PathVariable UUID marchandUuid) {
        log.info("Récupération des commandes du marchand: {}", marchandUuid);

        CommandeListResponseDTO response = commandeAdminApiAdapter.listerCommandesMarchand(marchandUuid);

        log.info("Marchand {} : {} commandes trouvées", marchandUuid, response.getCommandes().size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<CommandeListDTO> lireParUuid(@PathVariable UUID uuid) {
        log.info("Admin : Récupération de la commande par UUID: {}", uuid);

        CommandeListDTO response = commandeAdminApiAdapter.lireParUuid(uuid);

        if (response != null) {
            log.info("Admin : Commande trouvée pour UUID: {}", uuid);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Admin : Aucune commande trouvée pour UUID: {}", uuid);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Valider un payout par l'admin
     * Change le statut de la commande en PAYOUT et envoie un mail au vendeur
     *
     * @param commandeUuid UUID de la commande
     * @param dto DTO contenant la date de dépôt du payout
     * @return Commande mise à jour
     */
    @PostMapping("/{commandeUuid}/payout/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommandeListDTO> validerPayout(
            @PathVariable UUID commandeUuid,
            @RequestBody ValiderPayoutDTO dto) {
        log.info("Admin : Validation du payout pour la commande: {}", commandeUuid);
        
        CommandeListDTO response = commandeAdminApiAdapter.validerPayout(commandeUuid, dto);
        
        log.info("Admin : Payout validé pour la commande {}", commandeUuid);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Upload de la facture du vendeur
     * Accessible au vendeur (MARCHAND) et à l'admin
     *
     * @param commandeUuid UUID de la commande
     * @param facture Fichier de la facture
     * @return Commande mise à jour
     */
    @PostMapping("/{commandeUuid}/facture/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<CommandeListDTO> uploadFactureVendeur(
            @PathVariable UUID commandeUuid,
            @RequestParam("facture") MultipartFile facture) {
        log.info("Vendeur : Upload de facture pour la commande: {}", commandeUuid);
        
        CommandeListDTO response = commandeAdminApiAdapter.uploadFactureVendeur(commandeUuid, facture);
        
        log.info("Vendeur : Facture uploadée pour la commande {}", commandeUuid);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Valider les factures des clients par l'admin
     *
     * @param commandeUuid UUID de la commande
     * @param dto DTO contenant la liste des UUIDs des utilisateurs validés
     * @return Informations sur les validations
     */
    @PostMapping("/{commandeUuid}/factures/valider")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<ValidationFacturesClientResponseDTO> validerFacturesClients(
            @PathVariable UUID commandeUuid,
            @RequestBody ValiderFacturesClientDTO dto) {
        log.info("Admin : Validation des factures clients pour la commande: {}", commandeUuid);
        
        ValidationFacturesClientResponseDTO response = 
            commandeAdminApiAdapter.validerFacturesClients(commandeUuid, dto);
        
        log.info("Admin : Factures validées pour la commande {}", commandeUuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/command/{commandeUUid}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEUR')")
    public ResponseEntity<List<CommandeUtilisateurDto>> listerCommandesParUtilisateur(UUID commandeUUid) {
        log.info("Admin : Récupération des commandes pour la commande : {}", commandeUUid);

        List<CommandeUtilisateurDto> response = commandeAdminApiAdapter.listerUtilisateursParCommande(commandeUUid);

        log.info("Admin : {} commandes trouvées pour l'utilisateur {}", response.size(), commandeUUid);
        return ResponseEntity.ok(response);
    }

}

