package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.modele.ImageModele;
import com.ulr.paytogether.core.modele.PubliciteModele;
import com.ulr.paytogether.core.provider.PubliciteProvider;
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
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PubliciteServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class PubliciteServiceImplTest {

    @Mock
    private PubliciteProvider publiciteProvider;

    @InjectMocks
    private PubliciteServiceImpl publiciteService;

    private PubliciteModele publiciteModele;
    private UUID uuidPublicite;

    @BeforeEach
    void setUp() {
        uuidPublicite = UUID.randomUUID();

        publiciteModele = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité de test")
                .description("Description de la publicité")
                .lienExterne("https://example.com")
                .listeImages(List.of(
                        ImageModele.builder().urlImage("image1.jpg").build(),
                        ImageModele.builder().urlImage("image2.jpg").build()
                ))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
}

    @Test
    void testCreer_DevraitCreerPublicite() {
        // Given
        when(publiciteProvider.sauvegarder(any(PubliciteModele.class))).thenReturn(publiciteModele);

        // When
        PubliciteModele resultat = publiciteService.creer(publiciteModele);

        // Then
        assertNotNull(resultat);
        assertEquals("Publicité de test", resultat.getTitre());
        verify(publiciteProvider, times(1)).sauvegarder(publiciteModele);
    }

    @Test
    void testLireParUuid_DevraitRetournerPublicite() {
        // Given
        when(publiciteProvider.trouverParUuid(uuidPublicite)).thenReturn(Optional.of(publiciteModele));

        // When
        Optional<PubliciteModele> resultat = publiciteService.lireParUuid(uuidPublicite);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals(uuidPublicite, resultat.get().getUuid());
        verify(publiciteProvider, times(1)).trouverParUuid(uuidPublicite);
    }

    @Test
    void testLireParUuid_DevraitRetournerOptionalVide() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(publiciteProvider.trouverParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When
        Optional<PubliciteModele> resultat = publiciteService.lireParUuid(uuidInexistant);

        // Then
        assertFalse(resultat.isPresent());
        verify(publiciteProvider, times(1)).trouverParUuid(uuidInexistant);
    }

    @Test
    void testLireTous_DevraitRetournerToutesLesPublicites() {
        // Given
        PubliciteModele publicite2 = PubliciteModele.builder()
                .uuid(UUID.randomUUID())
                .titre("Publicité 2")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(15))
                .active(true)
                .build();

        List<PubliciteModele> publicites = Arrays.asList(publiciteModele, publicite2);
        when(publiciteProvider.trouverTous()).thenReturn(publicites);

        // When
        List<PubliciteModele> resultat = publiciteService.lireTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(publiciteProvider, times(1)).trouverTous();
    }

    @Test
    void testLireActives_DevraitRetournerPublicitesActives() {
        // Given
        List<PubliciteModele> publicites = List.of(publiciteModele);
        when(publiciteProvider.trouverActives()).thenReturn(publicites);

        // When
        List<PubliciteModele> resultat = publiciteService.lireActives();

        // Then
        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertTrue(resultat.get(0).getActive());
        verify(publiciteProvider, times(1)).trouverActives();
    }

    @Test
    void testMettreAJour_DevraitMettreAJourPublicite() {
        // Given
        PubliciteModele publiciteModifiee = PubliciteModele.builder()
                .uuid(uuidPublicite)
                .titre("Publicité modifiée")
                .description("Description modifiée")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(45))
                .active(false)
                .build();

        when(publiciteProvider.mettreAJour(uuidPublicite, publiciteModifiee)).thenReturn(publiciteModifiee);

        // When
        PubliciteModele resultat = publiciteService.mettreAJour(uuidPublicite, publiciteModifiee);

        // Then
        assertNotNull(resultat);
        assertEquals("Publicité modifiée", resultat.getTitre());
        verify(publiciteProvider, times(1)).mettreAJour(uuidPublicite, publiciteModifiee);
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerPublicite() {
        // Given
        doNothing().when(publiciteProvider).supprimerParUuid(uuidPublicite);

        // When
        publiciteService.supprimerParUuid(uuidPublicite);

        // Then
        verify(publiciteProvider, times(1)).supprimerParUuid(uuidPublicite);
    }
}
