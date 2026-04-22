package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.core.modele.PageModele;
import com.ulr.paytogether.core.provider.DealProvider;
import com.ulr.paytogether.provider.adapter.entity.*;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.provider.adapter.mapper.DealJpaMapper;
import com.ulr.paytogether.provider.repository.*;
import com.ulr.paytogether.provider.utils.AsyncPresignedUrlService;
import com.ulr.paytogether.provider.utils.FileManager;
import com.ulr.paytogether.provider.utils.Tools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA pour le deal
 * Implémente le port DealProvider défini dans bff-core
 * Fait le pont entre le domaine métier et la couche de persistence JPA
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DealProviderAdapter implements DealProvider {

    private final DealRepository jpaRepository;
    private final ImageDealRepository imageDealRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CategorieRepository categorieRepository;
    private final CommandeRepository commandeRepository;
    private final DealJpaMapper mapper;
    private final FileManager fileManager;
    private final CommentaireRepository commentaireRepository;
    private final AdresseRepository adresseRepository;
    private final PaiementRepository paiementRepository;
    private final AsyncPresignedUrlService asyncPresignedUrlService;


    // Invalide toutes les entrees de cache deals lors de la creation
    @Caching(evict = {
            @CacheEvict(value = "deals", allEntries = true),
            @CacheEvict(value = "deal",  allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DealModele sauvegarder(DealModele deal) {
        log.info("💾 Début de sauvegarde du deal: {}", deal.getTitre());

        // 1. ✅ Charger les entités JPA existantes depuis la BDD (pour éviter TransientPropertyValueException)
        UtilisateurJpa createurJpa = utilisateurRepository.findById(deal.getCreateur().getUuid())
                .orElseThrow(() -> new IllegalArgumentException("Créateur non trouvé pour l'UUID : " + deal.getCreateur().getUuid()));

        CategorieJpa categorieJpa = categorieRepository.findById(deal.getCategorie().getUuid())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée pour l'UUID : " + deal.getCategorie().getUuid()));

        // 2. Mapper le deal vers l'entité JPA
        DealJpa entite = mapper.versEntite(deal);

        // 3. ✅ Forcer l'assignation des entités chargées (remplace les entités "transient" créées par le mapper)
        entite.setMarchandJpa(createurJpa);
        entite.setCategorieJpa(categorieJpa);

        // 4. Gérer les images avec noms uniques et relation bidirectionnelle
        if (deal.getListeImages() != null && !deal.getListeImages().isEmpty()) {
            // Créer les images avec la relation bidirectionnelle
            List<ImageDealJpa> imageDealJpas = new ArrayList<>();

            for (ImageDealModele imageDealModele : deal.getListeImages()) {
                ImageDealJpa imageJpa = ImageDealJpa.builder()
                        .urlImage(FilenameUtils.getBaseName(imageDealModele.getUrlImage())
                                + "_" + System.currentTimeMillis()
                                + "." + FilenameUtils.getExtension(imageDealModele.getUrlImage()))
                        .isPrincipal(imageDealModele.getIsPrincipal())
                        .statut(imageDealModele.getStatut() != null ? imageDealModele.getStatut() : StatutImage.PENDING)
                        .dealJpa(entite)  // ✅ Relation bidirectionnelle
                        .build();
                imageDealJpas.add(imageJpa);
            }

            entite.setImageDealJpas(imageDealJpas);
        }

        // 5. ✅ Sauvegarder le deal (JPA cascade sauvegarde automatiquement les images grâce à CascadeType.ALL)
        log.info("💾 Sauvegarde en base de données avec cascade...");
        DealJpa sauvegarde = jpaRepository.save(entite);

        // 6. Mapper vers le modèle
        DealModele modeleSauvegarde = mapper.versModele(sauvegarde);

        // 7. Générer les URLs présignées pour les images PENDING
        setPresignUrl(modeleSauvegarde);

        log.info("🎉 Sauvegarde complète terminée - UUID: {}", modeleSauvegarde.getUuid());
        return modeleSauvegarde;
    }

    /**
     * Génère les URLs présignées pour les images PENDING en parallèle
     * Utilise les Virtual Threads pour optimiser les appels I/O vers MinIO
     */
    private void setPresignUrl(DealModele modeleSauvegarde) {
        if (modeleSauvegarde.getListeImages() == null || modeleSauvegarde.getListeImages().isEmpty()) {
            return;
        }

        // Filtrer les images PENDING
        var imagesPending = modeleSauvegarde.getListeImages().stream()
                .filter(img -> img.getStatut() == StatutImage.PENDING)
                .toList();

        if (imagesPending.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();

        // Génération en parallèle avec Virtual Threads via CompletableFuture
        List<String> fileNames = imagesPending.stream()
                .map(ImageDealModele::getUrlImage)
                .toList();

        List<String> presignedUrls = asyncPresignedUrlService.generatePresignedUrlsInParallel(
                Tools.DIRECTORY_DEALS_IMAGES,
                fileNames
        );

        // Assigner les URLs présignées aux images
        for (int i = 0; i < imagesPending.size(); i++) {
            imagesPending.get(i).setPresignUrl(presignedUrls.get(i));
        }

        long duration = System.currentTimeMillis() - start;
        log.debug("⚡ {} URLs présignées générées en {}ms (parallèle avec Virtual Threads)",
                  imagesPending.size(), duration);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<DealModele> trouverParUuid(UUID uuid) {
        return jpaRepository.findById(uuid)
                .map(mapper::versModele);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DealModele> trouverTous() {
        return jpaRepository.findAllByOrderByFavorisDescDateCreationDesc()
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public PageModele<DealModele> trouverTous(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "favoris", "dateCreation"));
        Page<DealJpa> pageJpa = jpaRepository.findAll(pageable);

        List<DealModele> content = pageJpa.getContent().stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());

        return PageModele.<DealModele>builder()
                .content(content)
                .page(pageJpa.getNumber())
                .size(pageJpa.getSize())
                .totalElements(pageJpa.getTotalElements())
                .totalPages(pageJpa.getTotalPages())
                .first(pageJpa.isFirst())
                .last(pageJpa.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<DealModele> trouverParStatut(StatutDeal statut) {
        return jpaRepository.findByStatutOrderByFavorisDescDateCreationDesc(statut)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public PageModele<DealModele> trouverParStatut(StatutDeal statut, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "favoris", "dateCreation"));
        Page<DealJpa> pageJpa = jpaRepository.findByStatut(statut, pageable);

        List<DealModele> content = pageJpa.getContent().stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());

        return PageModele.<DealModele>builder()
                .content(content)
                .page(pageJpa.getNumber())
                .size(pageJpa.getSize())
                .totalElements(pageJpa.getTotalElements())
                .totalPages(pageJpa.getTotalPages())
                .first(pageJpa.isFirst())
                .last(pageJpa.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<DealModele> trouverParCreateur(UUID createurUuid) {
        UtilisateurJpa marchandJpa = utilisateurRepository.findById(createurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + createurUuid));
        return jpaRepository.findByMarchandJpa(marchandJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public PageModele<DealModele> trouverParCreateur(UUID createurUuid, int page, int size) {
        UtilisateurJpa marchandJpa = utilisateurRepository.findById(createurUuid)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour l'UUID : " + createurUuid));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "favoris", "dateCreation"));
        Page<DealJpa> pageJpa = jpaRepository.findByMarchandJpa(marchandJpa, pageable);

        List<DealModele> content = pageJpa.getContent().stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());

        return PageModele.<DealModele>builder()
                .content(content)
                .page(pageJpa.getNumber())
                .size(pageJpa.getSize())
                .totalElements(pageJpa.getTotalElements())
                .totalPages(pageJpa.getTotalPages())
                .first(pageJpa.isFirst())
                .last(pageJpa.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<DealModele> trouverParCategorie(UUID categorieUuid) {
        CategorieJpa categorieJpa = categorieRepository.findById(categorieUuid)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée pour l'UUID : " + categorieUuid));

        return jpaRepository.findByCategorieJpa(categorieJpa)
                .stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public PageModele<DealModele> trouverParCategorie(UUID categorieUuid, int page, int size) {
        CategorieJpa categorieJpa = categorieRepository.findById(categorieUuid)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée pour l'UUID : " + categorieUuid));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "favoris", "dateCreation"));
        Page<DealJpa> pageJpa = jpaRepository.findByCategorieJpa(categorieJpa, pageable);

        List<DealModele> content = pageJpa.getContent().stream()
                .map(mapper::versModele)
                .collect(Collectors.toList());

        return PageModele.<DealModele>builder()
                .content(content)
                .page(pageJpa.getNumber())
                .size(pageJpa.getSize())
                .totalElements(pageJpa.getTotalElements())
                .totalPages(pageJpa.getTotalPages())
                .first(pageJpa.isFirst())
                .last(pageJpa.isLast())
                .build();
    }

    // Invalide la liste + l'entree specifique lors de la mise a jour
    @Caching(evict = {
            @CacheEvict(value = "deals", allEntries = true),
            @CacheEvict(value = "deal",  key = "#uuid.toString()")
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DealModele mettreAJour(UUID uuid, DealModele deal) {
        log.info("🔄 Début de mise à jour du deal: {}", uuid);
        
        // 1. Récupérer le deal existant
        DealJpa dealExistant = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));
        
        // 2. ✅ Charger les entités JPA existantes depuis la BDD (éviter TransientObjectException)
        UtilisateurJpa createurJpa = null;
        if (deal.getCreateur() != null && deal.getCreateur().getUuid() != null) {
            createurJpa = utilisateurRepository.findById(deal.getCreateur().getUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Créateur non trouvé pour l'UUID : " + deal.getCreateur().getUuid()));
        }
        
        CategorieJpa categorieJpa = null;
        if (deal.getCategorie() != null && deal.getCategorie().getUuid() != null) {
            categorieJpa = categorieRepository.findById(deal.getCategorie().getUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée pour l'UUID : " + deal.getCategorie().getUuid()));
        }
        
        // 3. Mettre à jour les champs via le mapper
        mapper.mettreAJour(dealExistant, deal);
        
        // 4. ✅ Forcer l'assignation des entités chargées (évite les instances transient)
        if (createurJpa != null) {
            dealExistant.setMarchandJpa(createurJpa);
        }
        if (categorieJpa != null) {
            dealExistant.setCategorieJpa(categorieJpa);
        }
        
        // 5. Sauvegarder
        log.info("💾 Sauvegarde de la mise à jour...");
        DealJpa sauvegarde = jpaRepository.save(dealExistant);
        
        log.info("🎉 Mise à jour terminée - UUID: {}", sauvegarde.getUuid());
        return mapper.versModele(sauvegarde);
    }

    // Invalide toutes les entrees de cache deals lors de la suppression
    @Caching(evict = {
            @CacheEvict(value = "deals", allEntries = true),
            @CacheEvict(value = "deal",  key = "#uuid.toString()")
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void supprimerParUuid(UUID uuid) {
        log.info("🗑️ Début de suppression du deal : {}", uuid);

        DealJpa deal = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));

        // Compter les commandes associées
        var commandes = commandeRepository.findByDealJpa(deal);

        log.info("✅ Deal trouvé - Participants: {}, Images: {}, Points forts: {}, Commentaires: {}, Commandes: {}",
                deal.getParticipants() != null ? deal.getParticipants().size() : 0,
                deal.getImageDealJpas() != null ? deal.getImageDealJpas().size() : 0,
                deal.getListePointsForts() != null ? deal.getListePointsForts().size() : 0,
                deal.getCommentaires() != null ? deal.getCommentaires().size() : 0,
                commandes.isPresent()? 1 : 0);

        // 1. Supprimer les commandes liées AVANT tout (relation OneToOne sans cascade inverse)
        if (commandes.isPresent()) {
            CommandeJpa commandeJpa = commandes.get();
            if (commandeJpa.getStatut() != StatutCommande.TERMINEE) {
                log.warn("⚠️ Commande {} est payée, suppression du deal interdite", commandeJpa.getUuid());
                throw new IllegalStateException("Impossible de supprimer le deal car une commande associée est déjà payée");
            }
            log.info("✅ Commandes supprimées et flush effectué");
            List<PaiementJpa> paiementJpas = commandeJpa.getPaiements();
            for (PaiementJpa paiement : paiementJpas) {
                // supprimer addresse
                adresseRepository.findByPaiementUuid(paiement.getUuid()).ifPresent(adresse -> {
                    log.info("🔄 Suppression de l'adresse {} liée au paiement {}", adresse.getUuid(), paiement.getUuid());
                    adresseRepository.delete(adresse);
                    adresseRepository.flush();
                    log.info("✅ Adresse supprimée et flush effectué");
                });
                log.info("🔄 Suppression du paiement {}", paiement.getUuid());
                // supprimer le paiement associé
                paiementRepository.delete(paiement);
                paiementRepository.flush();
                log.info("✅ Paiement supprimé et flush effectué");
            }
            log.info("🔄 Suppression de {} commandes", commandeJpa.getUuid());
            commandeRepository.delete(commandeJpa);
            commandeRepository.flush();
        }


        // 2. Vider la relation ManyToMany avec les participants
        if (deal.getParticipants() != null && !deal.getParticipants().isEmpty()) {
            log.info("🔄 Suppression de {} participants", deal.getParticipants().size());
            deal.getParticipants().clear();
        }

        // 3. Vider la liste des commentaires (OneToMany avec orphanRemoval)
        if (deal.getCommentaires() != null && !deal.getCommentaires().isEmpty()) {
            log.info("🔄 Suppression de {} commentaires", deal.getCommentaires().size());
            deal.getCommentaires().clear();
        }

        // 4. Vider la liste des points forts (ElementCollection)
        if (deal.getListePointsForts() != null && !deal.getListePointsForts().isEmpty()) {
            log.info("🔄 Suppression de {} points forts", deal.getListePointsForts().size());
            deal.getListePointsForts().clear();
        }

        // 5. Vider la liste des images (OneToMany avec orphanRemoval)
        if (deal.getImageDealJpas() != null && !deal.getImageDealJpas().isEmpty()) {
            log.info("🔄 Suppression de {} images", deal.getImageDealJpas().size());
            deal.getImageDealJpas().clear();
        }

        // 6. Sauvegarder et forcer le flush pour appliquer toutes les suppressions
        log.info("💾 Sauvegarde et flush des changements...");
        jpaRepository.saveAndFlush(deal);
        log.info("✅ Flush intermédiaire effectué");

        // 7. Supprimer le deal par son UUID et forcer le flush
        log.info("🗑️ Suppression finale du deal par UUID...");
        jpaRepository.deleteDeal(uuid);
        log.info("✅ Deal supprimé avec succès : {}", uuid);
    }

    @Caching(evict = {
            @CacheEvict(value = "deals", allEntries = true),
            @CacheEvict(value = "deal",  key = "#uuid.toString()")
    })
    @Override
    public DealModele mettreAJourStatut(UUID uuid, StatutDeal statut) {
        DealJpa deal = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));

        deal.setStatut(statut);
        DealJpa sauvegarde = jpaRepository.save(deal);
        return mapper.versModele(sauvegarde);
    }

    @Caching(evict = {
            @CacheEvict(value = "deals", allEntries = true),
            @CacheEvict(value = "deal",  key = "#uuid.toString()")
    })
    @Transactional(rollbackFor = Exception.class)
    @Override
    public DealModele basculerFavoris(UUID uuid) {
        log.info("🔄 Basculement du statut favoris pour le deal: {}", uuid);

        DealJpa deal = jpaRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + uuid));

        // Basculer le statut (true -> false, false -> true)
        deal.setFavoris(!deal.getFavoris());

        DealJpa sauvegarde = jpaRepository.save(deal);

        log.info("✅ Statut favoris mis à jour: {} pour le deal: {}", sauvegarde.getFavoris(), uuid);
        return mapper.versModele(sauvegarde);
    }

    @Caching(evict = {
            @CacheEvict(value = "deals", allEntries = true),
            @CacheEvict(value = "deal",  key = "#uuid.toString()")
    })
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
                    }
                }
            }
        }

        // dateModification du deal est gérée automatiquement par @UpdateTimestamp
        DealJpa sauvegarde = jpaRepository.save(deal);
        DealModele modeleSauvegarde = mapper.versModele(sauvegarde);

        // Générer les URL présignées UNIQUEMENT pour les nouvelles images
        nouvellesImagesAvecPresign.forEach(img -> {
            String presignUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_DEALS_IMAGES, img.getUrlImage());
            img.setPresignUrl(presignUrl);
        });

        // Remplacer la liste des images par uniquement les nouvelles images avec presignUrl
        modeleSauvegarde.setListeImages(nouvellesImagesAvecPresign);

        log.info("🎉 mettreAJourImages() terminé avec succès");
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
                            jpaRepository.save(deal);
                        },
                        () -> {
                            throw new IllegalArgumentException("Image non trouvée pour l'UUID : " + imageUuid);
                        }
                );
    }

    @Transactional(readOnly = true)
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

    @Override
    public void supprimerImagesNonPresentes(UUID dealUuid, List<UUID> uuidsAConserver) {
        DealJpa deal = jpaRepository.findById(dealUuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + dealUuid));

        if (deal.getImageDealJpas() != null && !deal.getImageDealJpas().isEmpty()) {
            deal.getImageDealJpas().removeIf(imageJpa ->
                !uuidsAConserver.contains(imageJpa.getUuid())
            );
            jpaRepository.save(deal);
            log.info("Images supprimées pour le deal {} - UUIDs conservés : {}", dealUuid, uuidsAConserver);
        }
    }

    @Override
    public ImageDealModele ajouterImage(UUID dealUuid, ImageDealModele image) {
        DealJpa deal = jpaRepository.findById(dealUuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + dealUuid));

        // Créer la nouvelle image avec un nom unique
        ImageDealJpa nouvelleImage = ImageDealJpa.builder()
                .urlImage(FilenameUtils.getBaseName(image.getUrlImage())
                        + "_" + System.currentTimeMillis()
                        + "." + FilenameUtils.getExtension(image.getUrlImage()))
                .isPrincipal(image.getIsPrincipal())
                .statut(StatutImage.PENDING)
                .dealJpa(deal)
                .build();

        ImageDealJpa nouvelleImageJpa = imageDealRepository.save(nouvelleImage);

        if (deal.getImageDealJpas() == null) {
            deal.setImageDealJpas(new ArrayList<>());
        }
        deal.getImageDealJpas().add(nouvelleImageJpa);
        log.info("Nouvelle image ajoutée pour le deal {} - UUID : {}", dealUuid, nouvelleImageJpa.getUuid());

        // Créer le modèle et générer l'URL présignée
        ImageDealModele imageModele = ImageDealModele.builder()
                .uuid(nouvelleImageJpa.getUuid())
                .urlImage(nouvelleImageJpa.getUrlImage())
                .isPrincipal(nouvelleImageJpa.getIsPrincipal())
                .statut(StatutImage.PENDING)
                .dealUuid(dealUuid)
                .build();

        String presignUrl = fileManager.generatePresignedUrl(Tools.DIRECTORY_DEALS_IMAGES, nouvelleImage.getUrlImage());
        imageModele.setPresignUrl(presignUrl);

        return imageModele;
    }

    @Override
    public ImageDealModele mettreAJourImageExistante(UUID dealUuid, UUID imageUuid, ImageDealModele image) {
        DealJpa deal = jpaRepository.findById(dealUuid)
                .orElseThrow(() -> new IllegalArgumentException("Deal non trouvé pour l'UUID : " + dealUuid));

        ImageDealJpa imageExistante = deal.getImageDealJpas().stream()
                .filter(img -> img.getUuid().equals(imageUuid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Image non trouvée pour l'UUID : " + imageUuid));

        // Mettre à jour uniquement les champs modifiables (isPrincipal, statut)
        boolean modifie = false;

        if (image.getIsPrincipal() != null && !image.getIsPrincipal().equals(imageExistante.getIsPrincipal())) {
            imageExistante.setIsPrincipal(image.getIsPrincipal());
            modifie = true;
        }

        if (image.getStatut() != null && !image.getStatut().equals(imageExistante.getStatut())) {
            imageExistante.setStatut(image.getStatut());
            modifie = true;
        }

        if (modifie) {
            imageDealRepository.save(imageExistante);
            log.info("Image {} du deal {} mise à jour", imageUuid, dealUuid);
        }

        return ImageDealModele.builder()
                .uuid(imageExistante.getUuid())
                .urlImage(imageExistante.getUrlImage())
                .isPrincipal(imageExistante.getIsPrincipal())
                .statut(imageExistante.getStatut())
                .dealUuid(dealUuid)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ImageDealModele> trouverImageParUuid(UUID dealUuid, UUID imageUuid) {
        return jpaRepository.findById(dealUuid)
                .flatMap(deal -> deal.getImageDealJpas().stream()
                        .filter(img -> img.getUuid().equals(imageUuid))
                        .findFirst()
                        .map(imageJpa -> ImageDealModele.builder()
                                .uuid(imageJpa.getUuid())
                                .urlImage(imageJpa.getUrlImage())
                                .isPrincipal(imageJpa.getIsPrincipal())
                                .statut(imageJpa.getStatut())
                                .dealUuid(dealUuid)
                                .build())
                );
    }

    @Transactional(readOnly = true)
    @Override
    public Double calculerMoyenneCommentaires(UUID dealUuid) {
        log.debug("Calcul de la moyenne des commentaires pour le deal: {}", dealUuid);
        Double moyenne = commentaireRepository.calculerMoyenneNotesPourDeal(dealUuid);
        return moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : null; // Arrondi à 1 décimale
    }

    @Transactional(readOnly = true)
    @Override
    public Long compterParticipantsReels(UUID dealUuid) {
        log.debug("Comptage des participants réels pour le deal: {}", dealUuid);
        return jpaRepository.findById(dealUuid)
                .map(deal -> (long) deal.getParticipants().size())
                .orElse(0L);
    }

    @Transactional(readOnly = true)
    @Override
    public Long calculerNombrePartsAchetees(UUID dealUuid) {
        log.debug("Calcul du nombre total de parts achetées pour le deal: {}", dealUuid);
        return jpaRepository.findById(dealUuid)
                .map(deal -> deal.getParticipants().stream()
                        .mapToLong(participant -> participant.getNombreDePart() != null ? participant.getNombreDePart() : 0)
                        .sum())
                .orElse(0L);
    }
}
