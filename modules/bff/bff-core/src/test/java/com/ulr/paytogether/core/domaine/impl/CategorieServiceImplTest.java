package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.provider.CategorieProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CategorieServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class CategorieServiceImplTest {

    @Mock
    private CategorieProvider categorieProvider;

    @InjectMocks
    private CategorieServiceImpl categorieService;

    private CategorieModele categorieModele;
    private UUID uuidCategorie;

    @BeforeEach
    void setUp() {
        uuidCategorie = UUID.randomUUID();

        categorieModele = CategorieModele.builder()
                .uuid(uuidCategorie)
                .nom("POISSON")
                .description("Produits de la mer")
                .icone("fish-icon.svg")
                .build();
    }

    @Test
    void testCreer_DevraitCreerCategorie() {
        // Given
        when(categorieProvider.existeParNom("POISSON")).thenReturn(false);
        when(categorieProvider.sauvegarder(any(CategorieModele.class))).thenReturn(categorieModele);

        // When
        CategorieModele resultat = categorieService.creer(categorieModele);

        // Then
        assertNotNull(resultat);
        assertEquals("POISSON", resultat.getNom());
        verify(categorieProvider, times(1)).existeParNom("POISSON");
        verify(categorieProvider, times(1)).sauvegarder(categorieModele);
    }

    @Test
    void testCreer_DevraitLancerExceptionSiNomExiste() {
        // Given
        when(categorieProvider.existeParNom("POISSON")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> categorieService.creer(categorieModele));
        verify(categorieProvider, times(1)).existeParNom("POISSON");
        verify(categorieProvider, never()).sauvegarder(any());
    }

    @Test
    void testLireParUuid_DevraitRetournerCategorie() {
        // Given
        when(categorieProvider.trouverParUuid(uuidCategorie)).thenReturn(Optional.of(categorieModele));

        // When
        Optional<CategorieModele> resultat = categorieService.lireParUuid(uuidCategorie);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("POISSON", resultat.get().getNom());
        verify(categorieProvider, times(1)).trouverParUuid(uuidCategorie);
    }

    @Test
    void testLireParNom_DevraitRetournerCategorie() {
        // Given
        when(categorieProvider.trouverParNom("POISSON")).thenReturn(Optional.of(categorieModele));

        // When
        Optional<CategorieModele> resultat = categorieService.lireParNom("POISSON");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("POISSON", resultat.get().getNom());
        verify(categorieProvider, times(1)).trouverParNom("POISSON");
    }

    @Test
    void testLireTous_DevraitRetournerToutesLesCategories() {
        // Given
        CategorieModele categorie2 = CategorieModele.builder()
                .uuid(UUID.randomUUID())
                .nom("VIANDE")
                .description("Produits carnés")
                .build();

        List<CategorieModele> categories = Arrays.asList(categorieModele, categorie2);
        when(categorieProvider.trouverTous()).thenReturn(categories);

        // When
        List<CategorieModele> resultat = categorieService.lireTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(categorieProvider, times(1)).trouverTous();
    }

    @Test
    void testMettreAJour_DevraitMettreAJourCategorie() {
        // Given
        CategorieModele categorieModifiee = CategorieModele.builder()
                .uuid(uuidCategorie)
                .nom("POISSON_FRAIS")
                .description("Poissons frais du jour")
                .build();

        when(categorieProvider.trouverParNom("POISSON_FRAIS")).thenReturn(Optional.empty());
        when(categorieProvider.mettreAJour(uuidCategorie, categorieModifiee)).thenReturn(categorieModifiee);

        // When
        CategorieModele resultat = categorieService.mettreAJour(uuidCategorie, categorieModifiee);

        // Then
        assertNotNull(resultat);
        assertEquals("POISSON_FRAIS", resultat.getNom());
        verify(categorieProvider, times(1)).mettreAJour(uuidCategorie, categorieModifiee);
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiNomExisteDeja() {
        // Given
        UUID autreUuid = UUID.randomUUID();
        CategorieModele autreCategorie = CategorieModele.builder()
                .uuid(autreUuid)
                .nom("VIANDE")
                .build();

        CategorieModele categorieModifiee = CategorieModele.builder()
                .uuid(uuidCategorie)
                .nom("VIANDE")
                .build();

        when(categorieProvider.trouverParNom("VIANDE")).thenReturn(Optional.of(autreCategorie));

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> categorieService.mettreAJour(uuidCategorie, categorieModifiee));
        verify(categorieProvider, never()).mettreAJour(any(), any());
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerCategorie() {
        // Given
        doNothing().when(categorieProvider).supprimerParUuid(uuidCategorie);

        // When
        categorieService.supprimerParUuid(uuidCategorie);

        // Then
        verify(categorieProvider, times(1)).supprimerParUuid(uuidCategorie);
    }

    @Test
    void testExisteParNom_DevraitRetournerTrue() {
        // Given
        when(categorieProvider.existeParNom("POISSON")).thenReturn(true);

        // When
        boolean resultat = categorieService.existeParNom("POISSON");

        // Then
        assertTrue(resultat);
        verify(categorieProvider, times(1)).existeParNom("POISSON");
    }

    @Test
    void testExisteParNom_DevraitRetournerFalse() {
        // Given
        when(categorieProvider.existeParNom("INEXISTANT")).thenReturn(false);

        // When
        boolean resultat = categorieService.existeParNom("INEXISTANT");

        // Then
        assertFalse(resultat);
        verify(categorieProvider, times(1)).existeParNom("INEXISTANT");
    }
}
