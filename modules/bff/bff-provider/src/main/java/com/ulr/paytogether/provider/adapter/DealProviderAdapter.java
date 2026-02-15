package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
                    mettreAJourImagesSiBesoin(jpa, deal);
                    return jpaRepository.save(jpa);
                })
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));

        DealModele modeleSauvegarde = mapper.versModele(entite);
        setPresignUrl(modeleSauvegarde);
        return modeleSauvegarde;
    }

    @Override
    public void supprimerParUuid(UUID uuid) {
        jpaRepository.deleteById(uuid);
    }

    private void mettreAJourImagesSiBesoin(DealJpa jpa, DealModele deal) {
        if (jpa.getImageDealJpas() == null || jpa.getImageDealJpas().isEmpty()) {
            return;
        }
        if (deal.getListeImages() == null || deal.getListeImages().isEmpty()) {
            return;
        }

        var imagesParUuid = deal.getListeImages().stream()
                .filter(image -> image.getUuid() != null)
                .collect(Collectors.toMap(
                        image -> image.getUuid(),
                        image -> image,
                        (image1, image2) -> image2
                ));

        jpa.getImageDealJpas().forEach(imageDealJpa -> {
            var imageEntrante = imagesParUuid.get(imageDealJpa.getUuid());
            if (imageEntrante == null) {
                return;
            }

            String urlEntrante = imageEntrante.getUrlImage();
            String urlActuelle = imageDealJpa.getUrlImage();

            if (urlEntrante != null && !urlEntrante.equals(urlActuelle)) {
                String nouvelleUrl = urlEntrante + "_" + System.currentTimeMillis();
                imageDealJpa.setUrlImage(nouvelleUrl);
                imageDealJpa.setStatut(StatutImage.PENDING);
            }
        });
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
                .map(image -> fileManager.generatePresignedUrlForRead(image.getUrlImage()))
                .orElseThrow(() -> new IllegalArgumentException("Image non trouvée pour l'UUID : " + imageUuid));
    }
}
