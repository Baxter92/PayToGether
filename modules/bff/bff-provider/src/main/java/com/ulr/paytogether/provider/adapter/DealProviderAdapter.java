package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.core.provider.DealProvider;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.provider.adapter.entity.ImageDealJpa;
import com.ulr.paytogether.provider.adapter.entity.UtilisateurJpa;
import com.ulr.paytogether.provider.adapter.mapper.DealJpaMapper;
import com.ulr.paytogether.provider.repository.CategorieRepository;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.repository.UtilisateurRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour le deal
 * Implémente le port DealProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Component
@RequiredArgsConstructor
public class DealProviderAdapter implements DealProvider {

    private final DealRepository jpaRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final DealJpaMapper mapper;
    private final FileManager fileManager;


    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele sauvegarder(DealModele deal) {
        DealJpa entite = mapper.versEntite(deal);

        // Mettre à jour les noms des fichiers associés de façon unique au deal avant de les sauvegarder
        if (deal.getListeImages() != null && !deal.getListeImages().isEmpty()) {
            List<ImageDealJpa> imageDealJpas = deal.getListeImages().stream()
                    .map(imageDealModele -> ImageDealJpa.builder()
                            .uuid(imageDealModele.getUuid())
                            .urlImage(FilenameUtils.getBaseName(imageDealModele.getUrlImage())
                                    + "_" + System.currentTimeMillis()
                                    + "." + FilenameUtils.getExtension(imageDealModele.getUrlImage()))
                            .isPrincipal(imageDealModele.getIsPrincipal())
                            .statut(imageDealModele.getStatut())
                            .dealJpa(entite)
                            .build())
                    .toList();
            entite.setImageDealJpas(imageDealJpas);
        }

        DealJpa sauvegarde = jpaRepository.save(entite);
        DealModele modeleSauvegarde = mapper.versModele(sauvegarde);

        setPresignUrl(modeleSauvegarde);

        return modeleSauvegarde;
    }

    private void setPresignUrl(DealModele modeleSauvegarde) {
        // Gérer les fichiers associés au deal (génération des URL présignées)
        if (modeleSauvegarde.getListeImages() != null && !modeleSauvegarde.getListeImages().isEmpty()) {
            modeleSauvegarde.getListeImages().stream()
                    .filter(imageDealModele -> imageDealModele.getStatut() == StatutImage.PENDING)
                    .forEach(imageDealModele -> {
                        String presignedUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_DEALS_IMAGES, imageDealModele.getUrlImage());
                        imageDealModele.setPresignUrl(presignedUrl);
            });
        }
    }

    @Override
    public Optional<DealModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Override
    public List<DealModele> trouverTous() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<DealModele> trouverParStatut(StatutDeal statut) {
        return jpaRepository.findByStatut(statut)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<DealModele> trouverParCreateur(UUID createurUuid) {
        UtilisateurJpa marchandJpa = utilisateurRepository.findById(createurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + createurUuid));
        return jpaRepository.findByMarchandJpa(marchandJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<DealModele> trouverParCategorie(UUID categorieUuid) {
        CategorieJpa categorieJpa = categorieRepository.findById(categorieUuid)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée pour l'UUID : " + categorieUuid));

        return jpaRepository.findByCategorieJpa(categorieJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        DealJpa entite = jpaRepository.findById(uuid)
                .map(jpa -> {
                    mapper.mettreAJour(jpa, deal);
                    return jpaRepository.save(jpa);
                })
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));

        return mapper.versModele(entite);
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele mettreAJourStatut(UUID uuid, StatutDeal statut) {
        DealJpa deal = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));

        deal.setStatut(statut);
        deal.setDateModification(java.time.LocalDateTime.now());

        DealJpa sauvegarde = jpaRepository.save(deal);
        return mapper.versModele(sauvegarde);
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public DealModele mettreAJourImages(UUID uuid, DealModele dealAvecNouvellesImages) {
        DealJpa deal = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));

        if (dealAvecNouvellesImages.getListeImages() == null || dealAvecNouvellesImages.getListeImages().isEmpty()) {
            return mapper.versModele(deal);
        }

        // Collecter les UUIDs des images envoyées dans le DTO
        List<UUID> uuidsEnvoyes = dealAvecNouvellesImages.getListeImages().stream()
                .map(ImageDealModele::getUuid)
                .filter(Objects::nonNull)
                .toList();

        // 1. SUPPRIMER les images en BD dont l'UUID n'est pas dans le DTO
        if (deal.getImageDealJpas() != null && !deal.getImageDealJpas().isEmpty()) {
            deal.getImageDealJpas().removeIf(imageJpa ->
                !uuidsEnvoyes.contains(imageJpa.getUuid())
            );
        }

        // 2. Créer une map des images existantes en BD
        var imagesExistantesParUuid = deal.getImageDealJpas() != null
            ? deal.getImageDealJpas().stream()
                .collect(Collectors.toMap(
                    ImageDealJpa::getUuid,
                    img -> img,
                    (img1, img2) -> img2
                ))
            : new HashMap<UUID, ImageDealJpa>();

        // Liste des nouvelles images à retourner avec presignUrl
        List<ImageDealModele> nouvellesImagesAvecPresign = new ArrayList<>();

        // 3. Parcourir les images du DTO
        for (var imageModele : dealAvecNouvellesImages.getListeImages()) {
            if (imageModele.getUuid() == null) {
                // 3.1. AJOUTER nouvelle image (UUID null)
                ImageDealJpa nouvelleImage = ImageDealJpa.builder()
                        .uuid(UUID.randomUUID())
                        .urlImage(FilenameUtils.getBaseName(imageModele.getUrlImage())
                                + "_" + System.currentTimeMillis()
                                + "." + FilenameUtils.getExtension(imageModele.getUrlImage()))
                        .isPrincipal(imageModele.getIsPrincipal())
                        .statut(StatutImage.PENDING)
                        .dealJpa(deal)
                        .build();

                if (deal.getImageDealJpas() == null) {
                    deal.setImageDealJpas(new java.util.ArrayList<>());
                }
                deal.getImageDealJpas().add(nouvelleImage);

                // Ajouter à la liste pour générer presignUrl
                ImageDealModele imageModeleAvecUuid =
                    ImageDealModele.builder()
                        .uuid(nouvelleImage.getUuid())
                        .urlImage(nouvelleImage.getUrlImage())
                        .isPrincipal(nouvelleImage.getIsPrincipal())
                        .statut(StatutImage.PENDING)
                        .build();
                nouvellesImagesAvecPresign.add(imageModeleAvecUuid);

            } else {
                // 3.2. MODIFIER image existante si isPrincipal change
                ImageDealJpa imageExistante = imagesExistantesParUuid.get(imageModele.getUuid());
                if (imageExistante != null) {
                    // Vérifier si isPrincipal a changé
                    if (imageExistante.getIsPrincipal() != imageModele.getIsPrincipal()) {
                        imageExistante.setIsPrincipal(imageModele.getIsPrincipal());
                        imageExistante.setDateModification(java.time.LocalDateTime.now());
                    }
                }
            }
        }

        deal.setDateModification(java.time.LocalDateTime.now());
        DealJpa sauvegarde = jpaRepository.save(deal);
        DealModele modeleSauvegarde = mapper.versModele(sauvegarde);

        // Générer les URL présignées UNIQUEMENT pour les nouvelles images
        nouvellesImagesAvecPresign.forEach(img -> {
            String presignUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_DEALS_IMAGES, img.getUrlImage());
            img.setPresignUrl(presignUrl);
        });

        // Remplacer la liste des images par uniquement les nouvelles images avec presignUrl
        modeleSauvegarde.setListeImages(nouvellesImagesAvecPresign);

        return modeleSauvegarde;
    }

    @Override
    public void mettreAJourStatutImage(UUID dealUuid, UUID imageUuid, StatutImage statut) {
        // Récupérer le deal
        DealJpa deal = jpaRepository.findById(dealUuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + dealUuid));

        // Trouver l'image et mettre à jour son statut
        deal.getImageDealJpas().stream()
                .filter(image -> image.getUuid().equals(imageUuid))
                .findFirst()
                .ifPresentOrElse(
                        image -> {
                            image.setStatut(statut);
                            image.setDateModification(java.time.LocalDateTime.now());
                            jpaRepository.save(deal);
                        },
                        () -> {
                            throw new IllegalArgumentException("Image non trouvée pour l'UUID : " + imageUuid);
                        }
                );
    }

    @Override
    public String obtenirUrlLectureImage(UUID dealUuid, UUID imageUuid) {
        // Récupérer le deal
        DealJpa deal = jpaRepository.findById(dealUuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + dealUuid));

        // Trouver l'image et générer l'URL de lecture
        return deal.getImageDealJpas().stream()
                .filter(image -> image.getUuid().equals(imageUuid))
                .findFirst()
                .map(image -> fileManager.generatePresignedUrlForRead(Tools.DIRECTORY_DEALS_IMAGES+image.getUrlImage()))
                .orElseThrow(() -> new IllegalArgumentException("Image non trouvée pour l'UUID : " + imageUuid));
    }
}
