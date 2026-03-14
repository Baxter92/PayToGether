package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
import com.ulr.paytogether.core.modele.DealAvecStatutModele;
import com.ulr.paytogether.core.modele.MarchandAvecDealsModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import com.ulr.paytogether.provider.adapter.entity.CommandeJpa;
import com.ulr.paytogether.provider.adapter.entity.CommentaireJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.UtilisateurJpaMapper;
import com.ulr.paytogether.provider.repository.CommandeRepository;
import com.ulr.paytogether.provider.repository.CommentaireRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import com.ulr.paytogether.wsclient.client.apiclient.AuthApiCLient;
import com.ulr.paytogether.wsclient.client.apiclient.UserApiClient;
import com.ulr.paytogether.wsclient.dto.LoginResponse;
import com.ulr.paytogether.wsclient.dto.UserRequest;
import com.ulr.paytogether.wsclient.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class UtilisateurProviderAdapter implements UtilisateurProvider {

    private final UtilisateurRepository jpaRepository;
    private final UtilisateurJpaMapper mapper;
    private final FileManager fileManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserApiClient userApiClient;
    private final AuthApiCLient authApiCLient;
    private final DealRepository dealRepository;
    private final CommandeRepository commandeRepository;
    private final CommentaireRepository commentaireRepository;

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
    public List<UtilisateurModele> trouverTousMarchands() {
        return jpaRepository.findByRole(RoleUtilisateur.VENDEUR)
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
                    utilisateurExistant.setUuid(uuid);

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
                .build();
        userApiClient.updateUser(token, sauvegarde.getKeycloakId(), userRequest);
    }

    @Override
    public void supprimerParUuid(UUID uuid, String token) {
        UtilisateurJpa utilisateurJpa = jpaRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'UUID: " + uuid));
        jpaRepository.deleteById(uuid);
        userApiClient.deleteUser(token, utilisateurJpa.getKeycloakId());
    }

    @Override
    public boolean existeParEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existeParKeycloakId(String keycloakId) {
        return jpaRepository.existsByKeycloakId(keycloakId);
    }

    @Override
    public Optional<UtilisateurModele> trouverParKeycloakId(String keycloakId) {
        return jpaRepository.findByKeycloakId(keycloakId)
                .map(mapper::versModele);
    }

    private void creerkeycloackutilisateur(UtilisateurJpa entite) {
        LoginResponse loginResponse = authApiCLient.loginAdmin();
        UserRequest userRequest = UserRequest.builder()
                .firstName(entite.getPrenom())
                .lastName(entite.getNom())
                .username(entite.getEmail())
                .email(entite.getEmail())
                .password(entite.getMotDePasse())
                .enabled(entite.getStatut() == StatutUtilisateur.ACTIF)
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
            }
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
        jpaRepository.save(utilisateur);
        LoginResponse loginResponse = authApiCLient.loginAdmin();
        // Réinitialiser le mot de passe dans Keycloak
        userApiClient.resetPassword(loginResponse.getAccessToken(), utilisateur.getKeycloakId(), nouveauMotDePasse);
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
        jpaRepository.save(utilisateur);

        LoginResponse loginResponse = authApiCLient.loginAdmin();
        // Activer/Désactiver l'utilisateur dans Keycloak
        userApiClient.enableUser(loginResponse.getAccessToken(), utilisateur.getKeycloakId(), actif);
    }

    @Transactional
    @Override
    public void assignerRole(UUID utilisateurUuid, String nomRole, String token) {
        // Récupérer l'utilisateur
        UtilisateurJpa utilisateur = jpaRepository.findById(utilisateurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + utilisateurUuid));

        RoleUtilisateur role = RoleUtilisateur.valueOf(nomRole);
        utilisateur.setRole(role);
        jpaRepository.save(utilisateur);
        // Assigner le rôle dans Keycloak
        userApiClient.assignRoleToUser(token, utilisateur.getKeycloakId(), nomRole);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MarchandAvecDealsModele> trouverTousMarchandsAvecDeals() {
        log.info("Provider - Récupération de tous les marchands avec leurs deals enrichis");
        
        // 1. Récupérer tous les marchands (role = VENDEUR)
        List<UtilisateurJpa> marchands = jpaRepository.findByRole(RoleUtilisateur.VENDEUR);
        
        // 2. Pour chaque marchand, récupérer ses deals avec statistiques
        return marchands.stream()
                .map(this::mapperMarchandAvecDeals)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapper un marchand avec ses deals enrichis
     */
    private MarchandAvecDealsModele mapperMarchandAvecDeals(UtilisateurJpa marchand) {
        // Récupérer tous les deals du marchand
        List<DealJpa> deals = dealRepository.findByMarchandJpa(marchand);
        
        // Mapper chaque deal avec ses statistiques
        List<DealAvecStatutModele> dealsAvecStatut = deals.stream()
                .map(this::mapperDealAvecStatut)
                .collect(Collectors.toList());
        
        // Calculer la moyenne globale du marchand
        Double moyenneGlobale = dealsAvecStatut.stream()
                .map(DealAvecStatutModele::getMoyenneCommentaires)
                .filter(moyenne -> moyenne != null && moyenne > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // Construire le modèle enrichi
        return MarchandAvecDealsModele.builder()
                .uuid(marchand.getUuid())
                .nom(marchand.getNom())
                .prenom(marchand.getPrenom())
                .email(marchand.getEmail())
                .statut(marchand.getStatut())
                .role(marchand.getRole())
                .photoProfil(marchand.getPhotoProfil() != null ? marchand.getPhotoProfil().getUrlImage() : null)
                .dateCreation(marchand.getDateCreation())
                .dateModification(marchand.getDateModification())
                .moyenneGlobale(moyenneGlobale)
                .nombreDeals(deals.size())
                .deals(dealsAvecStatut)
                .build();
    }
    
    /**
     * Mapper un deal avec son statut de commande et ses statistiques
     */
    private DealAvecStatutModele mapperDealAvecStatut(DealJpa deal) {
        // Récupérer la commande associée au deal
        Optional<CommandeJpa> commandeOpt = commandeRepository.findByDealJpa(deal);
        StatutCommande statutCommande = commandeOpt
                .map(CommandeJpa::getStatut)
                .orElse(null);
        
        // Calculer la moyenne des commentaires
        Double moyenneCommentaires = commentaireRepository.findByDealJpa(deal)
                .stream()
                .map(CommentaireJpa::getNote)
                .filter(note -> note != null && note > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        
        // Récupérer l'image principale
        String imageUrl = deal.getImageDealJpas() != null 
                ? deal.getImageDealJpas().stream()
                        .filter(img -> img.getIsPrincipal() != null && img.getIsPrincipal())
                        .findFirst()
                        .map(img -> fileManager.generatePresignedUrlForRead(img.getUrlImage()))
                        .orElse(null)
                : null;
        
        return DealAvecStatutModele.builder()
                .uuid(deal.getUuid())
                .titre(deal.getTitre())
                .description(deal.getDescription())
                .prixDeal(deal.getPrixDeal())
                .prixPart(deal.getPrixPart())
                .nbParticipants(deal.getNbParticipants())
                .dateDebut(deal.getDateDebut())
                .dateFin(deal.getDateFin())
                .statut(deal.getStatut())
                .ville(deal.getVille())
                .pays(deal.getPays())
                .dateCreation(deal.getDateCreation())
                .moyenneCommentaires(moyenneCommentaires)
                .nombreParticipantsReel(0L) // TODO: Calculer depuis les commandes
                .nombrePartsAchetees(0L) // TODO: Calculer depuis les commandes
                .statutCommande(statutCommande)
                .imageUrl(imageUrl)
                .build();
    }
}
