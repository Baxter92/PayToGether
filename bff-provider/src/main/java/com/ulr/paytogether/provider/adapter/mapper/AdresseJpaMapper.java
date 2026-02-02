package com.ulr.paytogether.provider.adapter.mapper;

import com.ulr.paytogether.core.modele.AdresseModele;
import com.ulr.paytogether.provider.adapter.entity.AdresseJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AdresseJpaMapper {
    private final UtilisateurJpaMapper utilisateurJpaMapper;

    public AdresseModele versModele(AdresseJpa jpaAdresse) {
        if (jpaAdresse == null) return null;
        return AdresseModele.builder()
                .uuid(jpaAdresse.getUuid())
                .rue(jpaAdresse.getRue())
                .ville(jpaAdresse.getVille())
                .codePostal(jpaAdresse.getCodePostal())
                .province(jpaAdresse.getProvince())
                .pays(jpaAdresse.getPays())
                .utilisateur(jpaAdresse.getUtilisateurJpa() != null ? utilisateurJpaMapper.versModele(jpaAdresse.getUtilisateurJpa()) : null)
                .dateCreation(jpaAdresse.getDateCreation())
                .dateModification(jpaAdresse.getDateModification())
                .build();
    }
    public AdresseJpa versEntite(AdresseModele modele) {
        if (modele == null) return null;
        return AdresseJpa.builder()
                .uuid(modele.getUuid())
                .rue(modele.getRue())
                .ville(modele.getVille())
                .codePostal(modele.getCodePostal())
                .province(modele.getProvince())
                .pays(modele.getPays())
                .utilisateurJpa(modele.getUtilisateur() != null ? utilisateurJpaMapper.versEntite(modele.getUtilisateur()) : null)
                .dateModification(modele.getDateModification())
                .build();
    }
}
