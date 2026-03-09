package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.*;
import com.ulr.paytogether.core.domaine.service.CommandeService;
import com.ulr.paytogether.core.modele.CommandeModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
}

