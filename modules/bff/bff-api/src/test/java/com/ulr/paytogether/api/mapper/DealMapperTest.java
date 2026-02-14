package com.ulr.paytogether.api.mapper;

import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.api.dto.DealResponseDto;
import com.ulr.paytogether.api.dto.ImageDealDto;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DealMapper
 */
@ExtendWith(MockitoExtension.class)
class DealMapperTest {

    @Mock
    private ImageDealMapper imageDealMapper;

    @InjectMocks
    private DealMapper dealMapper;

    private DealModele dealModele;
    private DealDTO dealDTO;
    private ImageDealModele imageDealModele;
    private ImageDealDto imageDealDto;

    @BeforeEach
    void setUp() {
        // Setup ImageDealModele
        imageDealModele = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image1.jpg")
                .isPrincipal(true)
                .statut(StatutImage.UPLOADED)
                .presignUrl("https://minio.exemple.com/presign")
                .build();

        // Setup ImageDealDto
        imageDealDto = new ImageDealDto(
                "image1.jpg",
                true,
                "https://minio.exemple.com/presign",
                StatutImage.UPLOADED
        );

        // Setup DealModele
        dealModele = DealModele.builder()
                .uuid(UUID.randomUUID())
                .titre("Deal Test")
                .description("Description du deal test")
                .prixDeal(new BigDecimal("100.00"))
                .prixPart(new BigDecimal("10.00"))
                .nbParticipants(10)
                .dateDebut(LocalDateTime.of(2026, 2, 14, 10, 0))
                .dateFin(LocalDateTime.of(2026, 12, 31, 23, 59))
                .statut(StatutDeal.PUBLIE)
                .createur(UtilisateurModele.builder()
                        .uuid(UUID.randomUUID())
                        .nom("Dupont")
                        .prenom("Jean")
                        .build())
                .categorie(CategorieModele.builder()
                        .uuid(UUID.randomUUID())
                        .nom("Viandes")
                        .build())
                .listeImages(List.of(imageDealModele))
                .listePointsForts(List.of("Point fort 1", "Point fort 2"))
                .dateExpiration(LocalDateTime.of(2026, 12, 31, 23, 59))
                .ville("Montreal")
                .pays("Canada")
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        // Setup DealDTO
        dealDTO = DealDTO.builder()
                .uuid(UUID.randomUUID())
                .titre("Deal DTO Test")
                .description("Description du deal DTO test")
                .prixDeal(new BigDecimal("200.00"))
                .prixPart(new BigDecimal("20.00"))
                .nbParticipants(10)
                .dateDebut(LocalDateTime.of(2026, 2, 14, 10, 0))
                .dateFin(LocalDateTime.of(2026, 12, 31, 23, 59))
                .statut(StatutDeal.BROUILLON)
                .createurUuid(UUID.randomUUID())
                .categorieUuid(UUID.randomUUID())
                .listeImages(List.of(imageDealDto))
                .listePointsForts(List.of("Point fort DTO"))
                .dateExpiration(LocalDateTime.of(2026, 12, 31, 23, 59))
                .ville("Quebec")
                .pays("Canada")
                .build();
    }

    @Test
    void versDTO_DevraitConvertirModeleEnDto() {
        // Given
        when(imageDealMapper.modeleVersDto(any(ImageDealModele.class))).thenReturn(imageDealDto);

        // When
        DealResponseDto resultat = dealMapper.versDTO(dealModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(dealModele.getUuid());
        assertThat(resultat.getTitre()).isEqualTo(dealModele.getTitre());
        assertThat(resultat.getDescription()).isEqualTo(dealModele.getDescription());
        assertThat(resultat.getPrixDeal()).isEqualByComparingTo(dealModele.getPrixDeal());
        assertThat(resultat.getPrixPart()).isEqualByComparingTo(dealModele.getPrixPart());
        assertThat(resultat.getNbParticipants()).isEqualTo(dealModele.getNbParticipants());
        assertThat(resultat.getStatut()).isEqualTo(dealModele.getStatut());
        assertThat(resultat.getCreateurUuid()).isEqualTo(dealModele.getCreateur().getUuid());
        assertThat(resultat.getCreateurNom()).isEqualTo("Dupont Jean");
        assertThat(resultat.getCategorieUuid()).isEqualTo(dealModele.getCategorie().getUuid());
        assertThat(resultat.getCategorieNom()).isEqualTo(dealModele.getCategorie().getNom());
        assertThat(resultat.getListeImages()).hasSize(1);
        assertThat(resultat.getVille()).isEqualTo(dealModele.getVille());
        assertThat(resultat.getPays()).isEqualTo(dealModele.getPays());

        verify(imageDealMapper, times(1)).modeleVersDto(any(ImageDealModele.class));
    }

    @Test
    void versDTO_DevraitRetournerNullQuandModeleNull() {
        // Given
        DealModele modele = null;

        // When
        DealResponseDto resultat = dealMapper.versDTO(modele);

        // Then
        assertThat(resultat).isNull();
        verify(imageDealMapper, never()).modeleVersDto(any());
    }

    @Test
    void versEntite_DevraitConvertirDtoEnModele() {
        // Given
        when(imageDealMapper.dtoVersModele(any(ImageDealDto.class))).thenReturn(imageDealModele);

        // When
        DealModele resultat = dealMapper.versEntite(dealDTO);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getTitre()).isEqualTo(dealDTO.getTitre());
        assertThat(resultat.getDescription()).isEqualTo(dealDTO.getDescription());
        assertThat(resultat.getPrixDeal()).isEqualByComparingTo(dealDTO.getPrixDeal());
        assertThat(resultat.getPrixPart()).isEqualByComparingTo(dealDTO.getPrixPart());
        assertThat(resultat.getNbParticipants()).isEqualTo(dealDTO.getNbParticipants());
        assertThat(resultat.getStatut()).isEqualTo(dealDTO.getStatut());
        assertThat(resultat.getCreateur()).isNotNull();
        assertThat(resultat.getCreateur().getUuid()).isEqualTo(dealDTO.getCreateurUuid());
        assertThat(resultat.getCategorie()).isNotNull();
        assertThat(resultat.getCategorie().getUuid()).isEqualTo(dealDTO.getCategorieUuid());
        assertThat(resultat.getListeImages()).hasSize(1);
        assertThat(resultat.getVille()).isEqualTo(dealDTO.getVille());
        assertThat(resultat.getPays()).isEqualTo(dealDTO.getPays());

        verify(imageDealMapper, times(1)).dtoVersModele(any(ImageDealDto.class));
    }

    @Test
    void versEntite_DevraitRetournerNullQuandDtoNull() {
        // Given
        DealDTO dto = null;

        // When
        DealModele resultat = dealMapper.versEntite(dto);

        // Then
        assertThat(resultat).isNull();
        verify(imageDealMapper, never()).dtoVersModele(any());
    }

    @Test
    void mettreAJour_DevraitMettreAJourModeleAvecDonneesDto() {
        // Given
        when(imageDealMapper.dtoVersModele(any(ImageDealDto.class))).thenReturn(imageDealModele);

        // When
        dealMapper.mettreAJour(dealModele, dealDTO);

        // Then
        assertThat(dealModele.getTitre()).isEqualTo(dealDTO.getTitre());
        assertThat(dealModele.getDescription()).isEqualTo(dealDTO.getDescription());
        assertThat(dealModele.getPrixDeal()).isEqualByComparingTo(dealDTO.getPrixDeal());
        assertThat(dealModele.getPrixPart()).isEqualByComparingTo(dealDTO.getPrixPart());
        assertThat(dealModele.getStatut()).isEqualTo(dealDTO.getStatut());
        assertThat(dealModele.getVille()).isEqualTo(dealDTO.getVille());
        assertThat(dealModele.getPays()).isEqualTo(dealDTO.getPays());

        verify(imageDealMapper, times(1)).dtoVersModele(any(ImageDealDto.class));
    }

    @Test
    void mettreAJour_NeShouldPasLeverExceptionQuandModeleNull() {
        // Given
        DealModele modele = null;

        // When & Then - ne doit pas lever d'exception
        dealMapper.mettreAJour(modele, dealDTO);

        verify(imageDealMapper, never()).dtoVersModele(any());
    }

    @Test
    void mettreAJour_NeShouldPasLeverExceptionQuandDtoNull() {
        // Given
        DealDTO dto = null;

        // When & Then - ne doit pas lever d'exception
        dealMapper.mettreAJour(dealModele, dto);

        verify(imageDealMapper, never()).dtoVersModele(any());
    }

    @Test
    void versDTO_DevraitGererListeImagesVide() {
        // Given
        dealModele.setListeImages(List.of());

        // When
        DealResponseDto resultat = dealMapper.versDTO(dealModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).isEmpty();
        verify(imageDealMapper, never()).modeleVersDto(any());
    }

    @Test
    void versDTO_DevraitGererListeImagesNull() {
        // Given
        dealModele.setListeImages(null);

        // When
        DealResponseDto resultat = dealMapper.versDTO(dealModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).isNull();
        verify(imageDealMapper, never()).modeleVersDto(any());
    }

    @Test
    void versEntite_DevraitGererListeImagesNull() {
        // Given
        dealDTO.setListeImages(null);

        // When
        DealModele resultat = dealMapper.versEntite(dealDTO);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getListeImages()).isNull();
        verify(imageDealMapper, never()).dtoVersModele(any());
    }

    @Test
    void versDTO_DevraitGererCreateurNull() {
        // Given
        dealModele.setCreateur(null);

        // When
        DealResponseDto resultat = dealMapper.versDTO(dealModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getCreateurUuid()).isNull();
        assertThat(resultat.getCreateurNom()).isNull();
    }

    @Test
    void versDTO_DevraitGererCategorieNull() {
        // Given
        dealModele.setCategorie(null);

        // When
        DealResponseDto resultat = dealMapper.versDTO(dealModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getCategorieUuid()).isNull();
        assertThat(resultat.getCategorieNom()).isNull();
    }
}

