package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.domaine.service.MinioEventProvider;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.MinioEvent;
import com.ulr.paytogether.provider.adapter.entity.ImageDealJpa;
import com.ulr.paytogether.provider.adapter.entity.ImageJpa;
import com.ulr.paytogether.provider.adapter.entity.ImageUtilisateurJpa;
import com.ulr.paytogether.provider.repository.ImageDealRepository;
import com.ulr.paytogether.provider.repository.ImageRepository;
import com.ulr.paytogether.provider.repository.ImageUtilisateurRepository;
import com.ulr.paytogether.provider.utils.Tools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioEventProviderAdapter implements MinioEventProvider {
    private final ImageDealRepository imageDealRepository;
    private final ImageUtilisateurRepository imageUtilisateurRepository;
    private final ImageRepository imageRepository;

    public void handleMinioEvent(MinioEvent event) {
        event.records().forEach(
                record -> {
                    String objectKey = record.s3().object().key();
                    String imageKey = objectKey.substring(objectKey.lastIndexOf('/') + 1);
                    if (objectKey.startsWith(Tools.DIRECTORY_DEALS_IMAGES)) {
                        ImageDealJpa imageDealJpa = imageDealRepository.findByUrlImage(imageKey);
                        imageDealJpa.setStatut(StatutImage.UPLOADED);
                        imageDealRepository.save(imageDealJpa);
                    } else if (objectKey.startsWith(Tools.DIRECTORY_PUBLICITES_IMAGES)) {
                        ImageJpa imageJpa = imageRepository.findByUrlImage(imageKey);
                        imageJpa.setStatut(StatutImage.UPLOADED);
                        imageRepository.save(imageJpa);
                    } else if (objectKey.startsWith(Tools.DIRECTORY_UTILISATEUR_IMAGES)) {
                        ImageUtilisateurJpa imageUtilisateurJpa = imageUtilisateurRepository.findByUrlImage(imageKey);
                        imageUtilisateurJpa.setStatut(StatutImage.UPLOADED);
                        imageUtilisateurRepository.save(imageUtilisateurJpa);
                    }
                }
        );
    }
}
