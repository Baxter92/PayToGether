package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentaireJpaMapper {
    private final UtilisateurJpaMapper utilisateurJpaMapper;
    private final DealJpaMapper dealJpaMapper;


    public CommentaireModele versModele(CommentaireJpa jpaCommentaire) {
        if (jpaCommentaire == null) return null;
        return CommentaireModele.builder()
                .uuid(jpaCommentaire.getUuid())
                .contenu(jpaCommentaire.getContenu())
                .note(jpaCommentaire.getNote())
                .utilisateur(jpaCommentaire.getUtilisateurJpa() != null ? utilisateurJpaMapper.versModele(jpaCommentaire.getUtilisateurJpa()) : null)
                .deal(jpaCommentaire.getDealJpa() != null ? dealJpaMapper.versModele(jpaCommentaire.getDealJpa()) : null)
                .dateCreation(jpaCommentaire.getDateCreation())
                .dateModification(jpaCommentaire.getDateModification())
                .build();
    }
    public CommentaireJpa versEntite(CommentaireModele modele) {
        if (modele == null) return null;
        return CommentaireJpa.builder()
                .uuid(modele.getUuid())
                .contenu(modele.getContenu())
                .note(modele.getNote())
                .utilisateurJpa(modele.getUtilisateur() != null ? utilisateurJpaMapper.versEntite(modele.getUtilisateur()) : null)
                .dealJpa(modele.getDeal() != null ? dealJpaMapper.versEntite(modele.getDeal()) : null)
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Met à jour une entité JPA existante avec les données du modèle
     */
    public void mettreAJour(CommentaireJpa jpa, CommentaireModele modele) {
        if (jpa == null || modele == null) return;

        jpa.setContenu(modele.getContenu());
        jpa.setNote(modele.getNote());

        // Ne pas mettre à jour les relations ni les dates (gérées par JPA)
    }
}
