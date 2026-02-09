package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CreerUtilisateurDTO;
import com.ulr.paytogether.api.dto.UtilisateurDTO;
import com.ulr.paytogether.api.mapper.UtilisateurMapper;
import com.ulr.paytogether.core.domaine.service.UtilisateurService;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ApiAdapter pour les utilisateurs
 * Fait le pont entre les Resources (API REST) et le Service Core
 * Gère la conversion entre DTO (API) et Modèle (Core)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UtilisateurApiAdapter {

    private final UtilisateurService utilisateurService;
    private final UtilisateurMapper mapper;

    /**
     * Créer un utilisateur
     */
    public UtilisateurDTO creer(CreerUtilisateurDTO dto) {
        log.info("ApiAdapter - Création d'un utilisateur: {}", dto.getEmail());

        UtilisateurModele modele = mapper.dtoVersModele(dto);
        UtilisateurModele cree = utilisateurService.creer(modele);

        return mapper.modeleVersDto(cree);
    }

    /**
     * Trouver un utilisateur par UUID
     */
    public Optional<UtilisateurDTO> trouverParUuid(UUID uuid) {
        log.debug("ApiAdapter - Recherche utilisateur par UUID: {}", uuid);

        return utilisateurService.lireParUuid(uuid)
                .map(mapper::modeleVersDto);
    }

    /**
     * Trouver un utilisateur par email
     */
    public Optional<UtilisateurDTO> trouverParEmail(String email) {
        log.debug("ApiAdapter - Recherche utilisateur par email: {}", email);

        return utilisateurService.lireParEmail(email)
                .map(mapper::modeleVersDto);
    }

    /**
     * Trouver tous les utilisateurs
     */
    public List<UtilisateurDTO> trouverTous() {
        log.debug("ApiAdapter - Recherche de tous les utilisateurs");

        return utilisateurService.lireTous()
                .stream()
                .map(mapper::modeleVersDto)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour un utilisateur
     */
    public UtilisateurDTO mettreAJour(UUID uuid, UtilisateurDTO dto) {
        log.info("ApiAdapter - Mise à jour utilisateur: {}", uuid);

        UtilisateurModele modele = mapper.dtoVersModele(dto);
        UtilisateurModele mis_a_jour = utilisateurService.mettreAJour(uuid, modele);

        return mapper.modeleVersDto(mis_a_jour);
    }

    /**
     * Supprimer un utilisateur
     */
    public void supprimer(UUID uuid) {
        log.info("ApiAdapter - Suppression utilisateur: {}", uuid);
        utilisateurService.supprimerParUuid(uuid);
    }

    /**
     * Vérifier si un email existe
     */
    public boolean existeParEmail(String email) {
        return utilisateurService.existeParEmail(email);
    }

    /**
     * Mettre à jour le statut de la photo de profil
     */
    public void mettreAJourStatutPhotoProfil(UUID utilisateurUuid, StatutImage statut) {
        utilisateurService.mettreAJourStatutPhotoProfil(utilisateurUuid, statut);
    }

    /**
     * Obtenir l'URL de lecture de la photo de profil
     */
    public String obtenirUrlLecturePhotoProfil(UUID utilisateurUuid) {
        return utilisateurService.obtenirUrlLecturePhotoProfil(utilisateurUuid);
    }
}
