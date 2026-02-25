package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.CommentaireDTO;
import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.springframework.stereotype.Component;

/**
 * Mapper entre CommentaireDTO et CommentaireModele
 * Fait le pont entre la couche API et le domaine métier
 */
@Component
public class CommentaireMapper {

    /**
     * Convertit un CommentaireModele en CommentaireDTO
     */
    public CommentaireDTO modeleVersDto(CommentaireModele modele) {
        if (modele == null) {
            return null;
        }

        return CommentaireDTO.builder()
                .uuid(modele.getUuid())
                .contenu(modele.getContenu())
                .note(modele.getNote())
                .utilisateurUuid(modele.getUtilisateur() != null ? modele.getUtilisateur().getUuid() : null)
                .dealUuid(modele.getDeal() != null ? modele.getDeal().getUuid() : null)
                .dateCreation(modele.getDateCreation())
                .dateModification(modele.getDateModification())
                .build();
    }

    /**
     * Convertit un CommentaireDTO en CommentaireModele
     */
    public CommentaireModele dtoVersModele(CommentaireDTO dto) {
        if (dto == null) {
            return null;
        }

        return CommentaireModele.builder()
                .uuid(dto.getUuid())
                .contenu(dto.getContenu())
                .note(dto.getNote())
                .utilisateur(dto.getUtilisateurUuid() != null
                    ? UtilisateurModele.builder().uuid(dto.getUtilisateurUuid()).build()
                    : null)
                .deal(dto.getDealUuid() != null
                    ? DealModele.builder().uuid(dto.getDealUuid()).build()
                    : null)
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
    }
}

