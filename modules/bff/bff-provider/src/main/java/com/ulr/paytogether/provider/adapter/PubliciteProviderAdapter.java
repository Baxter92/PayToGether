package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.ImageModele;
import com.ulr.paytogether.core.modele.PubliciteModele;
import com.ulr.paytogether.core.provider.PubliciteProvider;
import com.ulr.paytogether.provider.adapter.entity.ImageJpa;
import com.ulr.paytogether.provider.adapter.entity.PubliciteJpa;
import com.ulr.paytogether.provider.adapter.mapper.PubliciteJpaMapper;
import com.ulr.paytogether.provider.repository.PubliciteRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
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
            List<ImageJpa> imageJpas = publicite.getListeImages().stream()
                    .map(imageModele -> ImageJpa.builder()
                            .uuid(imageModele.getUuid())
                            .urlImage(FilenameUtils.getBaseName(imageModele.getUrlImage())
                                    + "_" + System.currentTimeMillis()
                                    + "." + FilenameUtils.getExtension(imageModele.getUrlImage()))
                            .statut(imageModele.getStatut())
                            .publiciteJpa(entite)
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
                    .filter(imageModele -> imageModele.getStatut() == StatutImage.PENDING)
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
                        ImageModele::getUuid,
                        image -> image
                ));

        jpa.getListeImages().forEach(imageJpa -> {
            var imageModele = imagesParUuid.get(imageJpa.getUuid());
            if (imageModele != null && !imageJpa.getUrlImage().equals(imageModele.getUrlImage())) {
                imageJpa.setUrlImage(imageModele.getUrlImage() + "_" + System.currentTimeMillis());
                imageJpa.setStatut(StatutImage.PENDING);
                imageJpa.setDateModification(java.time.LocalDateTime.now());
            }
        });
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Override
    public void mettreAJourStatutImage(UUID publiciteUuid, UUID imageUuid, StatutImage statut) {
        // Récupérer la publicité
        PubliciteJpa publicite = jpaRepository.findById(publiciteUuid)
                .orElseThrow(() -> new IllegalArgumentException("Publicité non trouvée pour l'UUID : " + publiciteUuid));

        // Trouver l'image et mettre à jour son statut
        publicite.getListeImages().stream()
                .filter(image -> image.getUuid().equals(imageUuid))
                .findFirst()
                .ifPresentOrElse(
                        image -> {
                            image.setStatut(statut);
                            image.setDateModification(java.time.LocalDateTime.now());
                            jpaRepository.save(publicite);
                        },
                        () -> {
                            throw new IllegalArgumentException("Image non trouvée pour l'UUID : " + imageUuid);
                        }
                );
    }

    @Override
    public String obtenirUrlLectureImage(UUID publiciteUuid, UUID imageUuid) {
        // Récupérer la publicité
        PubliciteJpa publicite = jpaRepository.findById(publiciteUuid)
                .orElseThrow(() -> new IllegalArgumentException("Publicité non trouvée pour l'UUID : " + publiciteUuid));

        // Trouver l'image et générer l'URL de lecture
        return publicite.getListeImages().stream()
                .filter(image -> image.getUuid().equals(imageUuid))
                .findFirst()
                .map(image -> fileManager.generatePresignedUrlForRead(image.getUrlImage()))
                .orElseThrow(() -> new IllegalArgumentException("Image non trouvée pour l'UUID : " + imageUuid));
    }
}
