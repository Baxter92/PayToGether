package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CreerUtilisateurDTO;
import com.ulr.paytogether.api.dto.MettreUtilisateurDto;
import com.ulr.paytogether.api.dto.ReinitialiserMotDePasseDTO;
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
 * Architecture hexagonale : PAS de logique métier ici, juste conversion + appel service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UtilisateurApiAdapter {

    private final UtilisateurService utilisateurService;
    private final UtilisateurMapper mapper;

    /**
     * Créer un utilisateur
     * Le service dispatche automatiquement un AccountValidationEvent
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
     * Trouver tous les utilisateurs
     */
    public List<UtilisateurDTO> trouverTousMarchands() {
        log.debug("ApiAdapter - Recherche de tous les utilisateurs marchands");

        return utilisateurService.lireTousMarchands()
                .stream()
                .map(mapper::modeleVersDto)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour un utilisateur
     */
    public UtilisateurDTO mettreAJour(UUID uuid, MettreUtilisateurDto dto, String token) {
        log.info("ApiAdapter - Mise à jour utilisateur: {}", uuid);

        UtilisateurModele modele = mapper.dtoVersModelePourMiseAJour(dto);
        UtilisateurModele mis_a_jour = utilisateurService.mettreAJour(uuid, modele, token);

        return mapper.modeleVersDto(mis_a_jour);
    }


    /**
     * Supprimer un utilisateur
     */
    public void supprimer(UUID uuid, String token) {
        log.info("ApiAdapter - Suppression utilisateur: {}", uuid);
        utilisateurService.supprimerParUuid(uuid, token);
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

    /**
     * Réinitialiser le mot de passe d'un utilisateur
     */
    public void reinitialiserMotDePasse(UUID utilisateurUuid, String nouveauMotDePasse, String token) {
        log.info("ApiAdapter - Réinitialisation du mot de passe pour l'utilisateur: {}", utilisateurUuid);
        utilisateurService.reinitialiserMotDePasse(utilisateurUuid, nouveauMotDePasse, token);
    }

    /**
     * Demander la réinitialisation du mot de passe (étape 1)
     * Envoie un événement qui déclenchera l'envoi d'un email avec un token
     */
    public void demanderReinitialisationMotDePasse(String email) {
        log.info("ApiAdapter - Demande de réinitialisation de mot de passe pour: {}", email);

        // Appel au service métier qui dispatche l'événement PasswordResetEvent
        utilisateurService.demanderReinitialisationMotDePasse(email);
    }

    /**
     * Réinitialiser le mot de passe avec validation du token
     * Conversion DTO → appel service métier (logique dans le core)
     */
    public void reinitialiserMotDePasseAvecToken(ReinitialiserMotDePasseDTO dto) {
        log.info("ApiAdapter - Réinitialisation du mot de passe avec token");

        // Pas de logique métier ici - juste appel au service
        utilisateurService.reinitialiserMotDePasseAvecToken(dto.getToken(), dto.getNouveauMotDePasse());
    }

    /**
     * Activer un compte utilisateur avec validation du token
     * Conversion → appel service métier (logique dans le core)
     */
    public void activerCompteAvecToken(String token) {
        log.info("ApiAdapter - Activation de compte avec token");

        // Pas de logique métier ici - juste appel au service
        utilisateurService.activerCompteAvecToken(token);
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    public void activerUtilisateur(UUID utilisateurUuid, boolean actif, String token) {
        log.info("ApiAdapter - Activation/Désactivation de l'utilisateur: {} - actif: {}", utilisateurUuid, actif);
        utilisateurService.activerUtilisateur(utilisateurUuid, actif, token);
    }

    /**
     * Assigner un rôle à un utilisateur
     */
    public void assignerRole(UUID utilisateurUuid, String nomRole, String token) {
        log.info("ApiAdapter - Assignation du rôle {} à l'utilisateur: {}", nomRole, utilisateurUuid);
        utilisateurService.assignerRole(utilisateurUuid, nomRole, token);
    }
}
