package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.UtilisateurJpaMapper;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import com.ulr.paytogether.wsclient.client.apiclient.AuthApiCLient;
import com.ulr.paytogether.wsclient.client.apiclient.UserApiClient;
import com.ulr.paytogether.wsclient.dto.LoginResponse;
import com.ulr.paytogether.wsclient.dto.UserRequest;
import com.ulr.paytogether.wsclient.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour l'utilisateur
 * Implémente le port UtilisateurPort défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class UtilisateurProviderAdapter implements UtilisateurProvider {

    private final UtilisateurRepository jpaRepository;
    private final UtilisateurJpaMapper mapper;
    private final FileManager fileManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserApiClient userApiClient;
    private final AuthApiCLient authApiCLient;

    @Transactional
    @Override
    public UtilisateurModele sauvegarder(UtilisateurModele utilisateur) {
        UtilisateurJpa entite = mapper.versEntite(utilisateur);

        // Créer l'utilisateur dans Keycloak avant de le sauvegarder localement
        creerkeycloackutilisateur(entite);
        // Hacher le mot de passe avec BCrypt si présent
        if (entite.getMotDePasse() != null && !entite.getMotDePasse().isEmpty()) {
            entite.setMotDePasse(passwordEncoder.encode(entite.getMotDePasse()));
        }
        if (entite.getPhotoProfil() != null) {
            entite.setPhotoProfilUnique(Tools.DIRECTORY_UTILISATEUR_IMAGES, entite.getPhotoProfil().getUrlImage());
        }
        UtilisateurModele modele = mapper.versModele(jpaRepository.save(entite));
        if (modele.getPhotoProfil() != null) {
            String presignUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_UTILISATEUR_IMAGES, modele.getPhotoProfil().getUrlImage());
            modele.setPresignUrlPhotoProfil(presignUrl);
        }
        return modele;
    }

    @Override
    public Optional<UtilisateurModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public Optional<UtilisateurModele> trouverParEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::versModele);
    }

    @Override
    public List<UtilisateurModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur, String token) {
        return jpaRepository.findById(uuid)
                .map(utilisateurExistant -> {
                    // Mettre à jour les champs modifiables
                    mapper.mettreAJour(utilisateurExistant, utilisateur);

                    // Mettre à jour l'utilisateur dans Keycloak
                    mettreAjourKeycloakUtilisateur(token, utilisateurExistant);

                    // Gérer la mise à jour de la photo de profil si nécessaire
                    mettreAJourPhotoProfilSiBesoin(utilisateurExistant, utilisateur);
                    // Sauvegarder et retourner le modèle mis à jour
                    UtilisateurJpa sauvegarde = jpaRepository.save(utilisateurExistant);
                    UtilisateurModele modele = mapper.versModele(sauvegarde);
                    setPresignUrl(modele);
                    return modele;
                })
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'UUID: " + uuid));
    }

    private void mettreAjourKeycloakUtilisateur(String token, UtilisateurJpa sauvegarde) {
        UserRequest userRequest = UserRequest.builder()
                .firstName(sauvegarde.getPrenom())
                .lastName(sauvegarde.getNom())
                .username(sauvegarde.getEmail())
                .build();
        userApiClient.updateUser(token, sauvegarde.getKeycloakId(), userRequest);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public boolean existeParEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private void creerkeycloackutilisateur(UtilisateurJpa entite) {
        LoginResponse loginResponse = authApiCLient.loginAdmin();
        UserRequest userRequest = UserRequest.builder()
                .firstName(entite.getPrenom())
                .lastName(entite.getNom())
                .username(entite.getEmail())
                .email(entite.getEmail())
                .password(entite.getMotDePasse())
                .build();
        UserResponse userResponse = userApiClient.createUser(loginResponse.getAccessToken(), userRequest);
        entite.setKeycloakId(userResponse.getId());
        userApiClient.assignRoleToUser(loginResponse.getAccessToken(), userResponse.getId(), entite.getRole().name());
    }

    private void mettreAJourPhotoProfilSiBesoin(UtilisateurJpa jpa, UtilisateurModele modele) {
        if (jpa.getPhotoProfil() != null && modele.getPhotoProfil() != null) {
            if (!jpa.getPhotoProfil().getUrlImage().equals(modele.getPhotoProfil().getUrlImage())) {
                jpa.getPhotoProfil().setUrlImage(modele.getPhotoProfil().getUrlImage());
                jpa.getPhotoProfil().setStatut(StatutImage.PENDING);
                jpa.getPhotoProfil().setDateModification(LocalDateTime.now());            }
        }
    }

    private void setPresignUrl(UtilisateurModele modeleSauvegarde) {
        // Gérer les fichiers associés au deal (génération des URL présignées)
        if (modeleSauvegarde.getPhotoProfil() != null && modeleSauvegarde.getPhotoProfil().getStatut() == StatutImage.PENDING) {
            String presignedUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_UTILISATEUR_IMAGES, modeleSauvegarde.getPhotoProfil().getUrlImage());
            modeleSauvegarde.setPresignUrlPhotoProfil(presignedUrl);
        }
    }

    @Override
    public void mettreAJourStatutPhotoProfil(UUID utilisateurUuid, StatutImage statut) {
        // Récupérer l'utilisateur
        UtilisateurJpa utilisateur = jpaRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        // Mettre à jour le statut de la photo de profil
        if (utilisateur.getPhotoProfil() != null) {
            utilisateur.getPhotoProfil().setStatut(statut);
            utilisateur.getPhotoProfil().setDateModification(LocalDateTime.now());
            jpaRepository.save(utilisateur);
        } else {
            throw new IllegalArgumentException("Aucune photo de profil trouvée pour cet utilisateur");
        }
    }

    @Override
    public String obtenirUrlLecturePhotoProfil(UUID utilisateurUuid) {
        // Récupérer l'utilisateur
        UtilisateurJpa utilisateur = jpaRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        // Générer l'URL de lecture pour la photo de profil
        if (utilisateur.getPhotoProfil() != null) {
            return fileManager.generatePresignedUrlForRead(Tools.DIRECTORY_UTILISATEUR_IMAGES+utilisateur.getPhotoProfil().getUrlImage());
        } else {
            throw new IllegalArgumentException("Aucune photo de profil trouvée pour cet utilisateur");
        }
    }

    @Transactional
    @Override
    public void reinitialiserMotDePasse(UUID utilisateurUuid, String nouveauMotDePasse, String token) {
        // Récupérer l'utilisateur
        UtilisateurJpa utilisateur = jpaRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        // Mettre à jour le mot de passe dans la base de données locale
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateur.setDateModification(LocalDateTime.now());
        jpaRepository.save(utilisateur);

        // Réinitialiser le mot de passe dans Keycloak
        userApiClient.resetPassword(token, utilisateur.getKeycloakId(), nouveauMotDePasse);
    }

    @Transactional
    @Override
    public void activerUtilisateur(UUID utilisateurUuid, boolean actif, String token) {
        // Récupérer l'utilisateur
        UtilisateurJpa utilisateur = jpaRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        // Mettre à jour le statut dans la base de données locale
        utilisateur.setStatut(actif ? StatutUtilisateur.ACTIF
                                    : StatutUtilisateur.INACTIF);
        utilisateur.setDateModification(LocalDateTime.now());
        jpaRepository.save(utilisateur);

        // Activer/Désactiver l'utilisateur dans Keycloak
        userApiClient.enableUser(token, utilisateur.getKeycloakId(), actif);
    }

    @Transactional
    @Override
    public void assignerRole(UUID utilisateurUuid, String nomRole, String token) {
        // Récupérer l'utilisateur
        UtilisateurJpa utilisateur = jpaRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        // Assigner le rôle dans Keycloak
        userApiClient.assignRoleToUser(token, utilisateur.getKeycloakId(), nomRole);
    }
}
