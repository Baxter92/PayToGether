package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.provider.DealProvider;
import com.ulr.paytogether.provider.adapter.entity.DealJpa;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.provider.adapter.entity.ImageDealJpa;
import com.ulr.paytogether.provider.adapter.mapper.DealJpaMapper;
import com.ulr.paytogether.provider.repository.DealRepository;
import com.ulr.paytogether.provider.utils.FileManager;
import lombok.RequiredArgsConstructor;
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
    private final DealJpaMapper mapper;
    private final FileManager fileManager;

    @Override
    public DealModele sauvegarder(DealModele deal) {
        DealJpa entite = mapper.versEntite(deal);

        // Mettre à jour les noms des fichiers associés de façon unique au deal avant de les sauvegarder
        if (deal.getListeImages() != null && !deal.getListeImages().isEmpty()) {
            List<ImageDealJpa> imageDealJpas = deal.getListeImages().stream()
                    .map(imageDealModele -> ImageDealJpa.builder()
                            .uuid(imageDealModele.getUuid())
                            .urlImage(imageDealModele.getUrlImage() + "_" + System.currentTimeMillis())
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
                        String presignedUrl = fileManager.generatePresignedUrl(imageDealModele.getUrlImage());
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
        return jpaRepository.findByCreateurUuid(createurUuid)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Override
    public List<DealModele> trouverParCategorie(UUID categorieUuid) {
        return jpaRepository.findByCategorieUuid(categorieUuid)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

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
}
