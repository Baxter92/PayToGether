package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.ImageDto;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.ImageModele;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour ImageMapper
 */
@ExtendWith(MockitoExtension.class)
class ImageMapperTest {

    @InjectMocks
    private ImageMapper mapper;

    @Test
    void modeleVersDto_DevraitConvertirModeleEnDto() {
        // Given
        ImageModele modele = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_test.jpg")
                .presignUrl("https://minio.exemple.com/presign-url")
                .statut(StatutImage.UPLOADED)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        // When
        ImageDto resultat = mapper.modeleVersDto(modele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.urlImage()).isEqualTo(modele.getUrlImage());
        assertThat(resultat.presignUrl()).isEqualTo(modele.getPresignUrl());
        assertThat(resultat.statut()).isEqualTo(modele.getStatut());
    }

    @Test
    void modeleVersDto_DevraitRetournerNullQuandModeleNull() {
        // Given
        ImageModele modele = null;

        // When
        ImageDto resultat = mapper.modeleVersDto(modele);

        // Then
        assertThat(resultat).isNull();
    }

    @Test
    void dtoVersModele_DevraitConvertirDtoEnModele() {
        // Given
        ImageDto dto = new ImageDto(
                "image_test.jpg",
                "https://minio.exemple.com/presign-url",
                StatutImage.PENDING
        );

        // When
        ImageModele resultat = mapper.dtoVersModele(dto);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUrlImage()).isEqualTo(dto.urlImage());
        assertThat(resultat.getPresignUrl()).isEqualTo(dto.presignUrl());
        assertThat(resultat.getStatut()).isEqualTo(dto.statut());
        assertThat(resultat.getUuid()).isNull();
        assertThat(resultat.getDateCreation()).isNull();
        assertThat(resultat.getDateModification()).isNull();
    }

    @Test
    void dtoVersModele_DevraitRetournerNullQuandDtoNull() {
        // Given
        ImageDto dto = null;

        // When
        ImageModele resultat = mapper.dtoVersModele(dto);

        // Then
        assertThat(resultat).isNull();
    }

    @Test
    void mettreAJour_DevraitMettreAJourModeleAvecDonneesDto() {
        // Given
        ImageModele modele = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_ancienne.jpg")
                .presignUrl("https://minio.exemple.com/old-url")
                .statut(StatutImage.PENDING)
                .dateCreation(LocalDateTime.now())
                .build();

        ImageDto dto = new ImageDto(
                "image_nouvelle.jpg",
                "https://minio.exemple.com/new-url",
                StatutImage.UPLOADED
        );

        // When
        mapper.mettreAJour(modele, dto);

        // Then
        assertThat(modele.getUrlImage()).isEqualTo(dto.urlImage());
        assertThat(modele.getPresignUrl()).isEqualTo(dto.presignUrl());
        assertThat(modele.getStatut()).isEqualTo(dto.statut());
        // Les champs non modifiables doivent rester inchangés
        assertThat(modele.getUuid()).isNotNull();
    }

    @Test
    void mettreAJour_NeShouldPasLeverExceptionQuandModeleNull() {
        // Given
        ImageModele modele = null;
        ImageDto dto = new ImageDto(
                "image.jpg",
                "https://minio.exemple.com/url",
                StatutImage.UPLOADED
        );

        // When & Then - ne doit pas lever d'exception
        mapper.mettreAJour(modele, dto);
    }

    @Test
    void mettreAJour_NeShouldPasLeverExceptionQuandDtoNull() {
        // Given
        ImageModele modele = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image.jpg")
                .statut(StatutImage.PENDING)
                .build();
        ImageDto dto = null;

        // When & Then - ne doit pas lever d'exception
        mapper.mettreAJour(modele, dto);
    }

    @Test
    void mettreAJour_DevraitGererValeursDtoPartielles() {
        // Given
        ImageModele modele = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image_originale.jpg")
                .presignUrl("https://minio.exemple.com/original-url")
                .statut(StatutImage.PENDING)
                .build();

        ImageDto dto = new ImageDto(
                "image_modifiee.jpg",
                null, // non modifié
                StatutImage.UPLOADED
        );

        // When
        mapper.mettreAJour(modele, dto);

        // Then
        assertThat(modele.getUrlImage()).isEqualTo("image_modifiee.jpg");
        assertThat(modele.getPresignUrl()).isEqualTo("https://minio.exemple.com/original-url"); // reste inchangé
        assertThat(modele.getStatut()).isEqualTo(StatutImage.UPLOADED);
    }

    @Test
    void modeleVersDto_DevraitGererValeursNullesDansModele() {
        // Given
        ImageModele modele = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image.jpg")
                .presignUrl(null)
                .statut(null)
                .build();

        // When
        ImageDto resultat = mapper.modeleVersDto(modele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.urlImage()).isEqualTo("image.jpg");
        assertThat(resultat.presignUrl()).isNull();
        assertThat(resultat.statut()).isNull();
    }

    @Test
    void dtoVersModele_DevraitGererValeursNullesDansDto() {
        // Given
        ImageDto dto = new ImageDto(null, null, null);

        // When
        ImageModele resultat = mapper.dtoVersModele(dto);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUrlImage()).isNull();
        assertThat(resultat.getPresignUrl()).isNull();
        assertThat(resultat.getStatut()).isEqualTo(StatutImage.PENDING);
    }
}

