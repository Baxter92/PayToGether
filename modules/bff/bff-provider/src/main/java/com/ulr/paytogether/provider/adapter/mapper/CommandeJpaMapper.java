package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandeJpaMapper {
    private final UtilisateurJpaMapper utilisateurJpaMapper;
    private final DealJpaMapper dealJpaMapper;
    private final PaiementJpaMapper paiementJpaMapper;

    public CommandeModele versModele(CommandeJpa jpaCommande) {
        if (jpaCommande == null) return null;
        return CommandeModele.builder()
                .uuid(jpaCommande.getUuid())
                .montantTotal(jpaCommande.getMontantTotal())
                .statut(jpaCommande.getStatut())
                .utilisateur(jpaCommande.getMarchandJpa() != null ?
                        utilisateurJpaMapper.versModele(jpaCommande.getMarchandJpa()) : null)
                .dealModele(jpaCommande.getDealJpa() != null ? dealJpaMapper.versModele(jpaCommande.getDealJpa()) : null)
                .paiements(jpaCommande.getPaiements() != null ?
                        jpaCommande.getPaiements().stream().map(
                                paiementJpaMapper::versModele
                        ).toList() : null)
                .dateCommande(jpaCommande.getDateCommande())
                .dateCreation(jpaCommande.getDateCreation())
                .dateModification(jpaCommande.getDateModification())
                .build();
    }
    public CommandeJpa versEntite(CommandeModele modele) {
        if (modele == null) return null;
        return CommandeJpa.builder()
                .uuid(modele.getUuid())
                .montantTotal(modele.getMontantTotal())
                .statut(modele.getStatut())
                .marchandJpa(modele.getUtilisateur() != null ?
                        utilisateurJpaMapper.versEntite(modele.getUtilisateur()) : null)
                .dealJpa(modele.getDealModele() != null ?
                        dealJpaMapper.versEntite(modele.getDealModele()) : null)
                .paiements(modele.getPaiements() != null ?
                        modele.getPaiements().stream().map(
                                paiementJpaMapper::versEntite
                        ).toList() : null)
                .dateCommande(modele.getDateCommande())
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }
}
