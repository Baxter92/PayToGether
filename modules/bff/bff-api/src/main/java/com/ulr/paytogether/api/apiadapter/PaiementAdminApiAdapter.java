package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.*;
import com.ulr.paytogether.core.domaine.service.PaiementService;
import com.ulr.paytogether.core.modele.PaiementModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ApiAdapter pour l'administration des paiements
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaiementAdminApiAdapter {

    private final PaiementService paiementService;

    /**
     * Récupère la liste complète des paiements avec statistiques
     * @return Réponse avec liste des paiements et statistiques
     */
    public PaiementListResponseDTO listerTousLesPaiements() {
        log.info("Récupération de la liste complète des paiements pour l'admin");

        // Récupérer tous les paiements avec infos complètes
        List<PaiementModele> paiements = paiementService.lireTousAvecInfosCompletes();

        // Mapper vers DTO
        List<PaiementListDTO> paiementsDTO = paiements.stream()
                .map(this::mapperVersPaiementListDTO)
                .collect(Collectors.toList());

        // Calculer les statistiques
        Map<String, Object> stats = paiementService.calculerStatistiques();
        PaiementStatsDTO statistiques = PaiementStatsDTO.builder()
                .totalTransactions((Long) stats.get("totalTransactions"))
                .transactionsReussies((Long) stats.get("transactionsReussies"))
                .transactionsEchouees((Long) stats.get("transactionsEchouees"))
                .montantTotal((BigDecimal) stats.get("montantTotal"))
                .build();

        return PaiementListResponseDTO.builder()
                .paiements(paiementsDTO)
                .statistiques(statistiques)
                .build();
    }

    /**
     * Mapper un PaiementModele vers PaiementListDTO
     */
    private PaiementListDTO mapperVersPaiementListDTO(PaiementModele paiement) {
        return PaiementListDTO.builder()
                .uuid(paiement.getUuid())
                // Client
                .clientUuid(paiement.getUtilisateur() != null ? paiement.getUtilisateur().getUuid() : null)
                .clientNom(paiement.getUtilisateur() != null ? paiement.getUtilisateur().getNom() : null)
                .clientPrenom(paiement.getUtilisateur() != null ? paiement.getUtilisateur().getPrenom() : null)
                .clientEmail(paiement.getUtilisateur() != null ? paiement.getUtilisateur().getEmail() : null)
                // Commande
                .commandeUuid(paiement.getCommande() != null ? paiement.getCommande().getUuid() : null)
                .numeroCommande(paiement.getCommande() != null ? paiement.getCommande().getNumeroCommande() : null)
                // Deal
                .dealUuid(paiement.getDeal() != null ? paiement.getDeal().getUuid() : null)
                .dealTitre(paiement.getDeal() != null ? paiement.getDeal().getTitre() : null)
                // Marchand
                .marchandUuid(paiement.getDeal() != null && paiement.getDeal().getCreateur() != null ?
                        paiement.getDeal().getCreateur().getUuid() : null)
                .marchandNom(paiement.getDeal() != null && paiement.getDeal().getCreateur() != null ?
                        paiement.getDeal().getCreateur().getNom() : null)
                .marchandPrenom(paiement.getDeal() != null && paiement.getDeal().getCreateur() != null ?
                        paiement.getDeal().getCreateur().getPrenom() : null)
                .marchandEmail(paiement.getDeal() != null && paiement.getDeal().getCreateur() != null ?
                        paiement.getDeal().getCreateur().getEmail() : null)
                // Détails paiement
                .montant(paiement.getMontant())
                .datePaiement(paiement.getDatePaiement())
                .nombreDePart(calculerNombreDePart(paiement))
                .methodePaiement(paiement.getMethodePaiement())
                .statutPaiement(paiement.getStatut())
                .build();
    }

    /**
     * Calcule le nombre de parts à partir du montant payé et du prix par part
     */
    private Integer calculerNombreDePart(PaiementModele paiement) {
        if (paiement.getDeal() != null && paiement.getDeal().getPrixPart() != null && paiement.getMontant() != null) {
            return paiement.getMontant().divide(paiement.getDeal().getPrixPart(), 0, java.math.RoundingMode.HALF_UP).intValue();
        }
        return null;
    }
}

