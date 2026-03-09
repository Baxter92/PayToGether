package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaiementJpaMapper {
    private final UtilisateurJpaMapper utilisateurJpaMapper;
    private final CommandeJpaMapper commandeJpaMapper;
    private final DealJpaMapper dealJpaMapper;

    public PaiementModele versModele(PaiementJpa jpaPaiement) {
        if (jpaPaiement == null) return null;
        return PaiementModele.builder()
                .uuid(jpaPaiement.getUuid())
                .montant(jpaPaiement.getMontant())
                .statut(jpaPaiement.getStatut())
                .methodePaiement(jpaPaiement.getMethodePaiement())
                .transactionId(jpaPaiement.getTransactionId())
                .squarePaymentId(jpaPaiement.getSquarePaymentId())
                .squareOrderId(jpaPaiement.getSquareOrderId())
                .squareLocationId(jpaPaiement.getSquareLocationId())
                .squareReceiptUrl(jpaPaiement.getSquareReceiptUrl())
                .squareToken(jpaPaiement.getSquareToken())
                .messageErreur(jpaPaiement.getMessageErreur())
                .utilisateur(jpaPaiement.getUtilisateurJpa() != null ? utilisateurJpaMapper.versModele(jpaPaiement.getUtilisateurJpa()) : null)
                .commande(jpaPaiement.getCommandeJpa() != null ? commandeJpaMapper.versModele(jpaPaiement.getCommandeJpa()) : null)
                .deal(jpaPaiement.getCommandeJpa() != null ? dealJpaMapper.versModele(jpaPaiement.getCommandeJpa().getDealJpa()) : null)
                .datePaiement(jpaPaiement.getDatePaiement())
                .dateCreation(jpaPaiement.getDateCreation())
                .dateModification(jpaPaiement.getDateModification())
                .build();
    }
    public PaiementJpa versEntite(PaiementModele modele) {
        if (modele == null) return null;
        return PaiementJpa.builder()
                .uuid(modele.getUuid())
                .montant(modele.getMontant())
                .statut(modele.getStatut())
                .methodePaiement(modele.getMethodePaiement())
                .transactionId(modele.getTransactionId())
                .squarePaymentId(modele.getSquarePaymentId())
                .squareOrderId(modele.getSquareOrderId())
                .squareLocationId(modele.getSquareLocationId())
                .squareReceiptUrl(modele.getSquareReceiptUrl())
                .squareToken(modele.getSquareToken())
                .messageErreur(modele.getMessageErreur())
                .utilisateurJpa(modele.getUtilisateur() != null ? utilisateurJpaMapper.versEntite(modele.getUtilisateur()) : null)
                .commandeJpa(modele.getCommande() != null ? commandeJpaMapper.versEntite(modele.getCommande()) : null)
                .datePaiement(modele.getDatePaiement()== null ? LocalDateTime.now() : modele.getDatePaiement())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Convertit une entité JPA en modèle complet avec toutes les informations enrichies
     * (utilisateur, commande avec numero, deal avec marchand)
     */
    public PaiementModele versModeleComplet(PaiementJpa jpaPaiement) {
        if (jpaPaiement == null) return null;

        PaiementModele paiement = versModele(jpaPaiement);

        // Enrichir avec le numéro de commande
        if (jpaPaiement.getCommandeJpa() != null && paiement.getCommande() != null) {
            paiement.getCommande().setNumeroCommande(jpaPaiement.getCommandeJpa().getNumeroCommande());

            // Enrichir avec le deal et le marchand
            if (jpaPaiement.getCommandeJpa().getDealJpa() != null) {
                paiement.setDeal(dealJpaMapper.versModele(jpaPaiement.getCommandeJpa().getDealJpa()));
            }
        }

        return paiement;
    }
}
