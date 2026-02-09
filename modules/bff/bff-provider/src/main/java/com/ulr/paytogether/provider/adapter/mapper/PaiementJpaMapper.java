package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.PaiementModele;
import com.ulr.paytogether.provider.adapter.entity.PaiementJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaiementJpaMapper {
    private final UtilisateurJpaMapper utilisateurJpaMapper;
    private final CommandeJpaMapper commandeJpaMapper;

    public PaiementModele versModele(PaiementJpa jpaPaiement) {
        if (jpaPaiement == null) return null;
        return PaiementModele.builder()
                .uuid(jpaPaiement.getUuid())
                .montant(jpaPaiement.getMontant())
                .statut(jpaPaiement.getStatut())
                .methodePaiement(jpaPaiement.getMethodePaiement())
                .transactionId(jpaPaiement.getTransactionId())
                .utilisateur(jpaPaiement.getUtilisateurJpa() != null ? utilisateurJpaMapper.versModele(jpaPaiement.getUtilisateurJpa()) : null)
                .commande(jpaPaiement.getCommandeJpa() != null ? commandeJpaMapper.versModele(jpaPaiement.getCommandeJpa()) : null)
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
                .utilisateurJpa(modele.getUtilisateur() != null ? utilisateurJpaMapper.versEntite(modele.getUtilisateur()) : null)
                .commandeJpa(modele.getCommande() != null ? commandeJpaMapper.versEntite(modele.getCommande()) : null)
                .datePaiement(modele.getDatePaiement())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }
}
