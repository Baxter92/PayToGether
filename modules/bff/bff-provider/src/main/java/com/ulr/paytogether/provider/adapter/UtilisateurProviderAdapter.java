package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.UtilisateurProvider;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.ImageUtilisateurJpaMapper;
import com.ulr.paytogether.provider.adapter.mapper.UtilisateurJpaMapper;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    private final ImageUtilisateurJpaMapper imageMapper;
    private final FileManager fileManager;

    @Override
    public UtilisateurModele sauvegarder(UtilisateurModele utilisateur) {
        UtilisateurJpa entite = mapper.versEntite(utilisateur);
        if (entite.getPhotoProfil() != null) {
            entite.setPhotoProfilUnique(entite.getPhotoProfil().getUrlImage());
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
    public UtilisateurModele mettreAJour(UUID uuid, UtilisateurModele utilisateur) {
        return jpaRepository.findById(uuid)
                .map(utilisateurExistant -> {
                    // Mettre à jour les champs modifiables
                    mapper.mettreAJour(utilisateurExistant, utilisateur);
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

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public boolean existeParEmail(String email) {
        return jpaRepository.existsByEmail(email);
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
}
