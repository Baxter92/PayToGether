package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.PubliciteModele;
import com.ulr.paytogether.core.provider.PubliciteProvider;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import com.ulr.paytogether.provider.adapter.mapper.PubliciteJpaMapper;
import com.ulr.paytogether.provider.repository.PubliciteRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour la publicité
 * Implémente le port PubliciteProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class PubliciteProviderAdapter implements PubliciteProvider {

    private final PubliciteRepository jpaRepository;
    private final PubliciteJpaMapper mapper;
    private final FileManager fileManager;

    @Override
    public PubliciteModele sauvegarder(PubliciteModele publicite) {
        PubliciteJpa entite = mapper.versEntite(publicite);

        // Mettre à jour les noms des fichiers associés de façon unique à la publicité avant de les sauvegarder
        if (publicite.getListeImages() != null && !publicite.getListeImages().isEmpty()) {
            List<com.ulr.paytogether.provider.adapter.entity.ImageJpa> imageJpas = publicite.getListeImages().stream()
                    .map(imageModele -> com.ulr.paytogether.provider.adapter.entity.ImageJpa.builder()
                            .uuid(imageModele.getUuid())
                            .urlImage(imageModele.getUrlImage() + "_" + System.currentTimeMillis())
                            .statut(imageModele.getStatut())
                            .build())
                    .toList();
            entite.setListeImages(imageJpas);
        }

        PubliciteJpa sauvegarde = jpaRepository.save(entite);
        PubliciteModele modeleSauvegarde = mapper.versModele(sauvegarde);

        setPresignUrl(modeleSauvegarde);

        return modeleSauvegarde;
    }

    private void setPresignUrl(PubliciteModele modeleSauvegarde) {
        // Gérer les fichiers associés à la publicité (génération des URL présignées)
        if (modeleSauvegarde.getListeImages() != null && !modeleSauvegarde.getListeImages().isEmpty()) {
            modeleSauvegarde.getListeImages().stream()
                    .filter(imageModele -> imageModele.getStatut() == com.ulr.paytogether.core.enumeration.StatutImage.PENDING)
                    .forEach(imageModele -> {
                        String presignedUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_PUBLICITES_IMAGES, imageModele.getUrlImage());
                        imageModele.setPresignUrl(presignedUrl);
                    });
        }
    }

    @Override
    public Optional<PubliciteModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public List<PubliciteModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<PubliciteModele> trouverActives() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public PubliciteModele mettreAJour(UUID uuid, PubliciteModele publicite) {
        PubliciteJpa entite = jpaRepository.findById(uuid)
                .map(publiciteExistante -> {
                    mapper.mettreAJour(publiciteExistante, publicite);
                    mettreAJourImagesSiBesoin(publiciteExistante, publicite);
                    return jpaRepository.save(publiciteExistante);
                })
                .orElseThrow(() -> new RuntimeException("Publicité non trouvée avec l'UUID: " + uuid));

        PubliciteModele modeleSauvegarde = mapper.versModele(entite);
        setPresignUrl(modeleSauvegarde);
        return modeleSauvegarde;
    }

    private void mettreAJourImagesSiBesoin(PubliciteJpa jpa, PubliciteModele publicite) {
        if (jpa.getListeImages() == null || jpa.getListeImages().isEmpty()) {
            return;
        }
        if (publicite.getListeImages() == null || publicite.getListeImages().isEmpty()) {
            return;
        }

        var imagesParUuid = publicite.getListeImages().stream()
                .filter(image -> image.getUuid() != null)
                .collect(Collectors.toMap(
                        com.ulr.paytogether.core.modele.ImageModele::getUuid,
                        image -> image
                ));

        jpa.getListeImages().forEach(imageJpa -> {
            var imageModele = imagesParUuid.get(imageJpa.getUuid());
            if (imageModele != null && !imageJpa.getUrlImage().equals(imageModele.getUrlImage())) {
                imageJpa.setUrlImage(imageModele.getUrlImage() + "_" + System.currentTimeMillis());
                imageJpa.setStatut(com.ulr.paytogether.core.enumeration.StatutImage.PENDING);
                imageJpa.setDateModification(java.time.LocalDateTime.now());
            }
        });
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }
}
