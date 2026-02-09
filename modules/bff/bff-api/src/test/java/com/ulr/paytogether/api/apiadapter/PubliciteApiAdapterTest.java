package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.PubliciteDTO;
import com.ulr.paytogether.api.mapper.PubliciteMapper;
import com.ulr.paytogether.core.domaine.service.PubliciteService;
import com.ulr.paytogether.core.modele.PubliciteModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PubliciteApiAdapter
 */
@ExtendWith(MockitoExtension.class)
class PubliciteApiAdapterTest {

    @Mock
    private PubliciteService publiciteService;

    @Mock
    private PubliciteMapper mapper;

    @InjectMocks
    private PubliciteApiAdapter apiAdapter;

    private PubliciteModele publiciteModele;
    private PubliciteDTO publiciteDTO;
    private UUID uuidPublicite;

    @BeforeEach
    void setUp() {
        uuidPublicite = UUID.randomUUID();

        publiciteModele = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité de test")
                .description("Description")
                .lienExterne("https://example.com")
                .listeImages(List.of(
                        com.ulr.paytogether.core.modele.ImageModele.builder()
                                .urlImage("image1.jpg")
                                .build()
                ))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        publiciteDTO = PubliciteDTO.builder()
                .uuid(uuidPublicite)
                .titre("Publicité de test")
                .description("Description")
                .lienExterne("https://example.com")
                .listeImages(List.of("image1.jpg"))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
    }

    @Test
    void testCreer_DevraitCreerPublicite() {
        // Given
        when(mapper.dtoVersModele(publiciteDTO)).thenReturn(publiciteModele);
        when(publiciteService.creer(publiciteModele)).thenReturn(publiciteModele);
        when(mapper.modeleVersDto(publiciteModele)).thenReturn(publiciteDTO);

        // When
        PubliciteDTO resultat = apiAdapter.creer(publiciteDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("Publicité de test", resultat.getTitre());
        verify(mapper, times(1)).dtoVersModele(publiciteDTO);
        verify(publiciteService, times(1)).creer(publiciteModele);
        verify(mapper, times(1)).modeleVersDto(publiciteModele);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerPublicite() {
        // Given
        when(publiciteService.lireParUuid(uuidPublicite)).thenReturn(Optional.of(publiciteModele));
        when(mapper.modeleVersDto(publiciteModele)).thenReturn(publiciteDTO);

        // When
        Optional<PubliciteDTO> resultat = apiAdapter.trouverParUuid(uuidPublicite);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("Publicité de test", resultat.get().getTitre());
        verify(publiciteService, times(1)).lireParUuid(uuidPublicite);
        verify(mapper, times(1)).modeleVersDto(publiciteModele);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(publiciteService.lireParUuid(uuidPublicite)).thenReturn(Optional.empty());

        // When
        Optional<PubliciteDTO> resultat = apiAdapter.trouverParUuid(uuidPublicite);

        // Then
        assertFalse(resultat.isPresent());
        verify(publiciteService, times(1)).lireParUuid(uuidPublicite);
        verify(mapper, never()).modeleVersDto(any());
    }

    @Test
    void testTrouverTous_DevraitRetournerListePublicites() {
        // Given
        PubliciteModele publiciteModele2 = PubliciteModele.builder()
                .uuid(UUID.randomUUID())
                .titre("Publicité 2")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(15))
                .active(false)
                .build();
        PubliciteDTO publiciteDTO2 = PubliciteDTO.builder()
                .uuid(publiciteModele2.getUuid())
                .titre("Publicité 2")
                .dateDebut(publiciteModele2.getDateDebut())
                .dateFin(publiciteModele2.getDateFin())
                .active(false)
                .build();

        when(publiciteService.lireTous()).thenReturn(Arrays.asList(publiciteModele, publiciteModele2));
        when(mapper.modeleVersDto(publiciteModele)).thenReturn(publiciteDTO);
        when(mapper.modeleVersDto(publiciteModele2)).thenReturn(publiciteDTO2);

        // When
        List<PubliciteDTO> resultat = apiAdapter.trouverTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(publiciteService, times(1)).lireTous();
        verify(mapper, times(2)).modeleVersDto(any(PubliciteModele.class));
    }

    @Test
    void testTrouverActives_DevraitRetournerPublicitesActives() {
        // Given
        when(publiciteService.lireActives()).thenReturn(List.of(publiciteModele));
        when(mapper.modeleVersDto(publiciteModele)).thenReturn(publiciteDTO);

        // When
        List<PubliciteDTO> resultat = apiAdapter.trouverActives();

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertTrue(resultat.get(0).getActive());
        verify(publiciteService, times(1)).lireActives();
        verify(mapper, times(1)).modeleVersDto(publiciteModele);
    }

    @Test
    void testMettreAJour_DevraitMettreAJourPublicite() {
        // Given
        PubliciteDTO publiciteModifieeDTO = PubliciteDTO.builder()
                .uuid(uuidPublicite)
                .titre("Titre modifié")
                .description("Description modifiée")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(45))
                .active(false)
                .build();
        PubliciteModele publiciteModifieeModele = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Titre modifié")
                .description("Description modifiée")
                .dateDebut(publiciteModifieeDTO.getDateDebut())
                .dateFin(publiciteModifieeDTO.getDateFin())
                .active(false)
                .build();

        when(mapper.dtoVersModele(publiciteModifieeDTO)).thenReturn(publiciteModifieeModele);
        when(publiciteService.mettreAJour(eq(uuidPublicite), any(PubliciteModele.class)))
                .thenReturn(publiciteModifieeModele);
        when(mapper.modeleVersDto(publiciteModifieeModele)).thenReturn(publiciteModifieeDTO);

        // When
        PubliciteDTO resultat = apiAdapter.mettreAJour(uuidPublicite, publiciteModifieeDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("Titre modifié", resultat.getTitre());
        verify(mapper, times(1)).dtoVersModele(publiciteModifieeDTO);
        verify(publiciteService, times(1)).mettreAJour(eq(uuidPublicite), any(PubliciteModele.class));
        verify(mapper, times(1)).modeleVersDto(publiciteModifieeModele);
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerPublicite() {
        // Given
        doNothing().when(publiciteService).supprimerParUuid(uuidPublicite);

        // When
        apiAdapter.supprimerParUuid(uuidPublicite);

        // Then
        verify(publiciteService, times(1)).supprimerParUuid(uuidPublicite);
    }
}
