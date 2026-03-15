package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.*;
import com.ulr.paytogether.core.domaine.service.CommandeService;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.CommandeUtilisateurModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ApiAdapter pour l'administration des commandes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommandeAdminApiAdapter {

    private final CommandeService commandeService;

    /**
     * Récupère la liste complète des commandes avec statistiques
     * @return Réponse avec liste des commandes et statistiques
     */
    public CommandeListResponseDTO listerToutesLesCommandes() {
        log.info("Récupération de la liste complète des commandes pour l'admin");

        // Récupérer toutes les commandes avec infos complètes
        List<CommandeModele> commandes = commandeService.lireToutesAvecInfosCompletes();

        // Mapper vers DTO
        List<CommandeListDTO> commandesDTO = commandes.stream()
                .map(this::mapperVersCommandeListDTO)
                .collect(Collectors.toList());

        // Calculer les statistiques
        Map<String, Long> stats = commandeService.calculerStatistiques();
        CommandeStatsDTO statistiques = CommandeStatsDTO.builder()
                .totalCommandes(stats.get("totalCommandes"))
                .commandesConfirmees(stats.get("commandesConfirmees"))
                .commandesEnCours(stats.get("commandesEnCours"))
                .commandesAnnulees(stats.get("commandesAnnulees"))
                .commandesRemboursees(stats.get("commandesRemboursees"))
                .build();

        return CommandeListResponseDTO.builder()
                .commandes(commandesDTO)
                .statistiques(statistiques)
                .build();
    }

    public CommandeListDTO lireParUuid(UUID uuid) {
        CommandeModele commande = commandeService.lireParUuid(uuid);
        return commande != null ? mapperVersCommandeListDTO(commande) : null;
    }

    /**
     * Récupère la liste des commandes d'un marchand avec statistiques
     * @param marchandUuid UUID du marchand
     * @return Réponse avec liste des commandes et statistiques
     */
    public CommandeListResponseDTO listerCommandesMarchand(UUID marchandUuid) {
        log.info("Récupération des commandes du marchand: {}", marchandUuid);

        // Récupérer toutes les commandes du marchand avec infos complètes
        List<CommandeModele> commandes = commandeService.lireCommandesMarchand(marchandUuid);

        // Mapper vers DTO
        List<CommandeListDTO> commandesDTO = commandes.stream()
                .map(this::mapperVersCommandeListDTO)
                .collect(Collectors.toList());

        // Calculer les statistiques pour ce marchand uniquement
        Map<String, Long> stats = calculerStatistiquesMarchand(commandes);
        CommandeStatsDTO statistiques = CommandeStatsDTO.builder()
                .totalCommandes(stats.get("totalCommandes"))
                .commandesConfirmees(stats.get("commandesConfirmees"))
                .commandesEnCours(stats.get("commandesEnCours"))
                .commandesAnnulees(stats.get("commandesAnnulees"))
                .commandesRemboursees(stats.get("commandesRemboursees"))
                .build();

        return CommandeListResponseDTO.builder()
                .commandes(commandesDTO)
                .statistiques(statistiques)
                .build();
    }

    /**
     * Calcule les statistiques des commandes d'un marchand
     */
    private Map<String, Long> calculerStatistiquesMarchand(List<CommandeModele> commandes) {
        long totalCommandes = commandes.size();
        long commandesConfirmees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.CONFIRMEE || c.getStatut() == StatutCommande.TERMINEE)
                .count();
        long commandesEnCours = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.EN_COURS ||
                             c.getStatut() == StatutCommande.COMPLETEE ||
                             c.getStatut() == StatutCommande.PAYOUT ||
                             c.getStatut() == StatutCommande.INVOICE_SELLER ||
                             c.getStatut() == StatutCommande.INVOICE_CUSTOMER)
                .count();
        long commandesAnnulees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.ANNULEE)
                .count();
        long commandesRemboursees = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.REMBOURSEE)
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCommandes", totalCommandes);
        stats.put("commandesConfirmees", commandesConfirmees);
        stats.put("commandesEnCours", commandesEnCours);
        stats.put("commandesAnnulees", commandesAnnulees);
        stats.put("commandesRemboursees", commandesRemboursees);

        return stats;
    }
    
    /**
     * Valider un payout par l'admin
     */
    public CommandeListDTO validerPayout(UUID commandeUuid, ValiderPayoutDTO dto) {
        log.info("Validation du payout pour la commande: {}", commandeUuid);
        
        CommandeModele commande = commandeService.validerPayout(
            commandeUuid, 
            dto.getDateDepotPayout()
        );
        
        return mapperVersCommandeListDTO(commande);
    }
    
    /**
     * Upload de la facture du vendeur
     */
    public CommandeListDTO uploadFactureVendeur(UUID commandeUuid, MultipartFile facture) {
        log.info("Upload de la facture du vendeur pour la commande: {}", commandeUuid);
        
        try {
            // Valider le fichier
            if (facture == null || facture.isEmpty()) {
                throw new IllegalArgumentException("Le fichier de facture est vide ou null");
            }
            
            // Convertir MultipartFile en byte[]
            byte[] factureData = facture.getBytes();
            String factureNom = facture.getOriginalFilename();
            
            CommandeModele commande = commandeService.uploadFactureVendeur(commandeUuid, factureData, factureNom);
            
            return mapperVersCommandeListDTO(commande);
        } catch (Exception e) {
            log.error("Erreur lors de l'upload de la facture: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'upload de la facture", e);
        }
    }
    
    /**
     * Valider les factures des clients
     */
    public ValidationFacturesClientResponseDTO validerFacturesClients(
            UUID commandeUuid, 
            ValiderFacturesClientDTO dto) {
        log.info("Validation des factures clients pour la commande: {}", commandeUuid);
        
        Map<String, Object> resultat = commandeService.validerFacturesClients(
            commandeUuid, 
            dto.getUtilisateurUuids()
        );
        
        return ValidationFacturesClientResponseDTO.builder()
            .commandeUuid((UUID) resultat.get("commandeUuid"))
            .numeroCommande((String) resultat.get("numeroCommande"))
            .nombreValidations(((Long) resultat.get("nombreValidations")).intValue())
            .nombreTotal(((Integer) resultat.get("nombreTotal")))
            .toutesValidees((Boolean) resultat.get("toutesValidees"))
            .message((String) resultat.get("message"))
            .build();
    }

    public List<CommandeUtilisateurDto> listerUtilisateursParCommande(UUID commandeUuid) {
        List<CommandeUtilisateurModele> cuList = commandeService.listerUtilisateursParCommande(commandeUuid);
        return cuList.stream()
                .map(this::mapperVersCommandeUtilisateurDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapper un CommandeModele vers CommandeListDTO
     */
    private CommandeListDTO mapperVersCommandeListDTO(CommandeModele commande) {
        return CommandeListDTO.builder()
                .uuid(commande.getUuid())
                .numeroCommande(commande.getNumeroCommande())
                // Marchand
                .marchandUuid(commande.getUtilisateur() != null ? commande.getUtilisateur().getUuid() : null)
                .marchandNom(commande.getUtilisateur() != null ? commande.getUtilisateur().getNom() : null)
                .marchandPrenom(commande.getUtilisateur() != null ? commande.getUtilisateur().getPrenom() : null)
                .marchandEmail(commande.getUtilisateur() != null ? commande.getUtilisateur().getEmail() : null)
                // Deal
                .dealUuid(commande.getDealModele() != null ? commande.getDealModele().getUuid() : null)
                .dealTitre(commande.getDealModele() != null ? commande.getDealModele().getTitre() : null)
                // Détails commande
                .dateCreation(commande.getDateCreation())
                .montantTotalPaiements(commande.getMontantTotalPaiements())
                .statut(commande.getStatut())
                .build();
    }

    private CommandeUtilisateurDto mapperVersCommandeUtilisateurDTO(CommandeUtilisateurModele cu) {
        return CommandeUtilisateurDto.builder()
                .uuid(cu.getUuid())
                .commandeUuid(cu.getCommandeUuid())
                .utilisateurUuid(cu.getUtilisateurUuid())
                .nom(cu.getUtilisateur() != null ? cu.getUtilisateur().getNom() : null)
                .prenom(cu.getUtilisateur() != null ? cu.getUtilisateur().getPrenom() : null)
                .email(cu.getUtilisateur() != null ? cu.getUtilisateur().getEmail() : null)
                .statutCommandeUtilisateur(cu.getStatutCommandeUtilisateur().name())
                .build();
    }
}

