package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.ImageDto;
import com.ulr.paytogether.api.dto.PubliciteDTO;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.ImageModele;
import com.ulr.paytogether.core.modele.PubliciteModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PubliciteMapper
 */
@ExtendWith(MockitoExtension.class)
class PubliciteMapperTest {

    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private PubliciteMapper publiciteMapper;

    private PubliciteModele publiciteModele;
    private PubliciteDTO publiciteDTO;
    private ImageModele imageModele;
    private ImageDto imageDto;

    @BeforeEach
    void setUp() {
        // Setup ImageModele
        imageModele = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("pub_image.jpg")
                .presignUrl("https://minio.exemple.com/presign")
                .statut(StatutImage.UPLOADED)
                .build();

        // Setup ImageDto
        imageDto = new ImageDto(
                UUID.randomUUID(),
                "pub_image.jpg",
                "https://minio.exemple.com/presign",
                StatutImage.UPLOADED
        );

        // Setup PubliciteModele
        publiciteModele = PubliciteModele.builder()
                .uuid(UUID.randomUUID())
                .titre("Publicité Test")
                .description("Description de la publicité test")
                .lienExterne("https://example.com")
                .listeImages(List.of(imageModele))
                .dateDebut(LocalDateTime.of(2026, 2, 14, 10, 0))
                .dateFin(LocalDateTime.of(2026, 3, 31, 23, 59))
                .active(true)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        // Setup PubliciteDTO
        publiciteDTO = PubliciteDTO.builder()
                .uuid(UUID.randomUUID())
                .titre("Publicité DTO Test")
                .description("Description de la publicité DTO test")
                .lienExterne("https://example.com/promo")
                .listeImages(List.of(imageDto))
                .dateDebut(LocalDateTime.of(2026, 2, 14, 10, 0))
                .dateFin(LocalDateTime.of(2026, 3, 31, 23, 59))
                .active(true)
                .build();
    }

    @Test
    void modeleVersDto_DevraitConvertirModeleEnDto() {
        // Given
        when(imageMapper.modeleVersDto(any(ImageModele.class))).thenReturn(imageDto);

        // When
        PubliciteDTO resultat = publiciteMapper.modeleVersDto(publiciteModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(publiciteModele.getUuid());
        assertThat(resultat.getTitre()).isEqualTo(publiciteModele.getTitre());
        assertThat(resultat.getDescription()).isEqualTo(publiciteModele.getDescription());
        assertThat(resultat.getLienExterne()).isEqualTo(publiciteModele.getLienExterne());
        assertThat(resultat.getListeImages()).hasSize(1);
        assertThat(resultat.getDateDebut()).isEqualTo(publiciteModele.getDateDebut());
        assertThat(resultat.getDateFin()).isEqualTo(publiciteModele.getDateFin());
        assertThat(resultat.getActive()).isEqualTo(publiciteModele.getActive());

        verify(imageMapper, times(1)).modeleVersDto(any(ImageModele.class));
    }

    @Test
    void modeleVersDto_DevraitRetournerNullQuandModeleNull() {
        // Given
        PubliciteModele modele = null;

        // When
        PubliciteDTO resultat = publiciteMapper.modeleVersDto(modele);

        // Then
        assertThat(resultat).isNull();
        verify(imageMapper, never()).modeleVersDto(any());
    }

    @Test
    void dtoVersModele_DevraitConvertirDtoEnModele() {
        // Given
        when(imageMapper.dtoVersModele(any(ImageDto.class))).thenReturn(imageModele);

        // When
        PubliciteModele resultat = publiciteMapper.dtoVersModele(publiciteDTO);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(publiciteDTO.getUuid());
        assertThat(resultat.getTitre()).isEqualTo(publiciteDTO.getTitre());
        assertThat(resultat.getDescription()).isEqualTo(publiciteDTO.getDescription());
        assertThat(resultat.getLienExterne()).isEqualTo(publiciteDTO.getLienExterne());
        assertThat(resultat.getListeImages()).hasSize(1);
        assertThat(resultat.getDateDebut()).isEqualTo(publiciteDTO.getDateDebut());
        assertThat(resultat.getDateFin()).isEqualTo(publiciteDTO.getDateFin());
        assertThat(resultat.getActive()).isEqualTo(publiciteDTO.getActive());

        verify(imageMapper, times(1)).dtoVersModele(any(ImageDto.class));
    }

    @Test
    void dtoVersModele_DevraitRetournerNullQuandDtoNull() {
        // Given
        PubliciteDTO dto = null;

        // When
        PubliciteModele resultat = publiciteMapper.dtoVersModele(dto);

        // Then
        assertThat(resultat).isNull();
        verify(imageMapper, never()).dtoVersModele(any());
    }

    @Test
    void modeleVersDto_DevraitGererListeImagesVide() {
        // Given
        publiciteModele.setListeImages(List.of());

        // When
        PubliciteDTO resultat = publiciteMapper.modeleVersDto(publiciteModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).isEmpty();
        verify(imageMapper, never()).modeleVersDto(any());
    }

    @Test
    void modeleVersDto_DevraitGererListeImagesNull() {
        // Given
        publiciteModele.setListeImages(null);

        // When
        PubliciteDTO resultat = publiciteMapper.modeleVersDto(publiciteModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).isNull();
        verify(imageMapper, never()).modeleVersDto(any());
    }

    @Test
    void dtoVersModele_DevraitGererListeImagesNull() {
        // Given
        publiciteDTO.setListeImages(null);

        // When
        PubliciteModele resultat = publiciteMapper.dtoVersModele(publiciteDTO);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).isNull();
        verify(imageMapper, never()).dtoVersModele(any());
    }

    @Test
    void modeleVersDto_DevraitMapperPlusieursImages() {
        // Given
        ImageModele image2 = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("pub_image2.jpg")
                .build();

        ImageDto imageDto2 = new ImageDto(UUID.randomUUID(), "pub_image2.jpg", null, null);

        publiciteModele.setListeImages(List.of(imageModele, image2));

        when(imageMapper.modeleVersDto(imageModele)).thenReturn(imageDto);
        when(imageMapper.modeleVersDto(image2)).thenReturn(imageDto2);

        // When
        PubliciteDTO resultat = publiciteMapper.modeleVersDto(publiciteModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).hasSize(2);
        verify(imageMapper, times(2)).modeleVersDto(any(ImageModele.class));
    }

    @Test
    void dtoVersModele_DevraitMapperPlusieursImages() {
        // Given
        ImageDto imageDto2 = new ImageDto(UUID.randomUUID(), "pub_image2.jpg", null, null);

        ImageModele image2 = ImageModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("pub_image2.jpg")
                .build();

        publiciteDTO.setListeImages(List.of(imageDto, imageDto2));

        when(imageMapper.dtoVersModele(imageDto)).thenReturn(imageModele);
        when(imageMapper.dtoVersModele(imageDto2)).thenReturn(image2);

        // When
        PubliciteModele resultat = publiciteMapper.dtoVersModele(publiciteDTO);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).hasSize(2);
        verify(imageMapper, times(2)).dtoVersModele(any(ImageDto.class));
    }

    @Test
    void modeleVersDto_DevraitGererLienExterneNull() {
        // Given
        publiciteModele.setLienExterne(null);

        // When
        PubliciteDTO resultat = publiciteMapper.modeleVersDto(publiciteModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getLienExterne()).isNull();
    }

    @Test
    void dtoVersModele_DevraitGererLienExterneNull() {
        // Given
        publiciteDTO.setLienExterne(null);

        // When
        PubliciteModele resultat = publiciteMapper.dtoVersModele(publiciteDTO);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getLienExterne()).isNull();
    }
}

