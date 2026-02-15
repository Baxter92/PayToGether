package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.ImageDealDto;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.ImageDealModele;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour ImageDealMapper
 */
@ExtendWith(MockitoExtension.class)
class ImageDealMapperTest {

    @InjectMocks
    private ImageDealMapper mapper;

    @Test
    void modeleVersDto_DevraitConvertirModeleEnDto() {
        // Given
        ImageDealModele modele = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_deal_123.jpg")
                .dealUuid(UUID.randomUUID())
                .isPrincipal(true)
                .statut(StatutImage.UPLOADED)
                .presignUrl("https://minio.exemple.com/presign-url")
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        // When
        ImageDealDto resultat = mapper.modeleVersDto(modele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.imageUuid()).isEqualTo(modele.getUuid());
        assertThat(resultat.urlImage()).isEqualTo(modele.getUrlImage());
        assertThat(resultat.isPrincipal()).isEqualTo(modele.getIsPrincipal());
        assertThat(resultat.presignUrl()).isEqualTo(modele.getPresignUrl());
        assertThat(resultat.statut()).isEqualTo(modele.getStatut());
    }

    @Test
    void modeleVersDto_DevraitRetournerNullQuandModeleNull() {
        // Given
        ImageDealModele modele = null;

        // When
        ImageDealDto resultat = mapper.modeleVersDto(modele);

        // Then
        assertThat(resultat).isNull();
    }

    @Test
    void dtoVersModele_DevraitConvertirDtoEnModele() {
        // Given
        ImageDealDto dto = new ImageDealDto(
                UUID.randomUUID(),
                "image_deal_456.jpg",
                false,
                "https://minio.exemple.com/presign-url-2",
                StatutImage.PENDING
        );

        // When
        ImageDealModele resultat = mapper.dtoVersModele(dto);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(dto.imageUuid());
        assertThat(resultat.getUrlImage()).isEqualTo(dto.urlImage());
        assertThat(resultat.getIsPrincipal()).isEqualTo(dto.isPrincipal());
        assertThat(resultat.getPresignUrl()).isEqualTo(dto.presignUrl());
        assertThat(resultat.getStatut()).isEqualTo(dto.statut());
        assertThat(resultat.getDealUuid()).isNull();
        assertThat(resultat.getDateCreation()).isNull();
        assertThat(resultat.getDateModification()).isNull();
    }

    @Test
    void dtoVersModele_DevraitRetournerNullQuandDtoNull() {
        // Given
        ImageDealDto dto = null;

        // When
        ImageDealModele resultat = mapper.dtoVersModele(dto);

        // Then
        assertThat(resultat).isNull();
    }

    @Test
    void mettreAJour_DevraitMettreAJourModeleAvecDonneesDto() {
        var uuid = UUID.randomUUID();
        // Given
        ImageDealModele modele = ImageDealModele.builder()
                .uuid(uuid)
                .urlImage("image_ancienne.jpg")
                .dealUuid(UUID.randomUUID())
                .isPrincipal(false)
                .statut(StatutImage.PENDING)
                .presignUrl("https://minio.exemple.com/old-url")
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        ImageDealDto dto = new ImageDealDto(
                uuid,
                "image_nouvelle.jpg",
                true,
                "https://minio.exemple.com/new-url",
                StatutImage.UPLOADED
        );

        // When
        mapper.mettreAJour(modele, dto);

        // Then
        assertThat(modele.getUuid()).isEqualTo(dto.imageUuid());
        assertThat(modele.getUrlImage()).isEqualTo(dto.urlImage());
        assertThat(modele.getIsPrincipal()).isEqualTo(dto.isPrincipal());
        assertThat(modele.getPresignUrl()).isEqualTo(dto.presignUrl());
        assertThat(modele.getStatut()).isEqualTo(dto.statut());
        // Les champs non modifiables doivent rester inchangés
        assertThat(modele.getUuid()).isNotNull();
        assertThat(modele.getDealUuid()).isNotNull();
    }

    @Test
    void mettreAJour_NeShouldPasLeverExceptionQuandModeleNull() {
        // Given
        ImageDealModele modele = null;
        ImageDealDto dto = new ImageDealDto(
                UUID.randomUUID(),
                "image.jpg",
                true,
                "https://minio.exemple.com/url",
                StatutImage.UPLOADED
        );

        // When & Then - ne doit pas lever d'exception
        mapper.mettreAJour(modele, dto);
    }

    @Test
    void mettreAJour_NeShouldPasLeverExceptionQuandDtoNull() {
        // Given
        ImageDealModele modele = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image.jpg")
                .isPrincipal(false)
                .statut(StatutImage.PENDING)
                .build();
        ImageDealDto dto = null;

        // When & Then - ne doit pas lever d'exception
        mapper.mettreAJour(modele, dto);
    }

    @Test
    void mettreAJour_DevraitGererValeursDtoPartielles() {
        var uuid = UUID.randomUUID();
        // Given
        ImageDealModele modele = ImageDealModele.builder()
                .uuid(uuid)
                .urlImage("image_originale.jpg")
                .isPrincipal(false)
                .statut(StatutImage.PENDING)
                .presignUrl("https://minio.exemple.com/original-url")
                .build();

        ImageDealDto dto = new ImageDealDto(
                uuid,
                "image_modifiee.jpg",
                null, // non modifié
                null, // non modifié
                StatutImage.UPLOADED
        );

        // When
        mapper.mettreAJour(modele, dto);

        // Then
        assertThat(modele.getUuid()).isEqualTo(uuid);
        assertThat(modele.getUrlImage()).isEqualTo("image_modifiee.jpg");
        assertThat(modele.getIsPrincipal()).isFalse(); // reste inchangé
        assertThat(modele.getPresignUrl()).isEqualTo("https://minio.exemple.com/original-url"); // reste inchangé
        assertThat(modele.getStatut()).isEqualTo(StatutImage.UPLOADED);
    }

    @Test
    void modeleVersDto_DevraitGererValeursNullesDansModele() {
        // Given
        ImageDealModele modele = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image.jpg")
                .isPrincipal(null)
                .statut(null)
                .presignUrl(null)
                .build();

        // When
        ImageDealDto resultat = mapper.modeleVersDto(modele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.urlImage()).isEqualTo("image.jpg");
        assertThat(resultat.isPrincipal()).isNull();
        assertThat(resultat.presignUrl()).isNull();
        assertThat(resultat.statut()).isNull();
    }

    @Test
    void dtoVersModele_DevraitGererValeursNullesDansDto() {
        // Given
        ImageDealDto dto = new ImageDealDto(
                null,
                null,
                null,
                null,
                null
        );

        // When
        ImageDealModele resultat = mapper.dtoVersModele(dto);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isNull();
        assertThat(resultat.getUrlImage()).isNull();
        assertThat(resultat.getIsPrincipal()).isNull();
        assertThat(resultat.getPresignUrl()).isNull();
        assertThat(resultat.getStatut()).isEqualTo(StatutImage.PENDING);
    }
}

