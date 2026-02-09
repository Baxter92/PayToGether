package com.ulr.paytogether.provider.adapter;

import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.provider.adapter.entity.CategorieJpa;
import com.ulr.paytogether.provider.adapter.mapper.CategorieJpaMapper;
import com.ulr.paytogether.provider.repository.CategorieRepository;
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
 * Tests unitaires pour CategorieProviderAdapter
 */
@ExtendWith(MockitoExtension.class)
class CategorieProviderAdapterTest {

    @Mock
    private CategorieRepository jpaRepository;

    @Mock
    private CategorieJpaMapper mapper;

    @InjectMocks
    private CategorieProviderAdapter providerAdapter;

    private CategorieModele categorieModele;
    private CategorieJpa categorieJpa;
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

        categorieJpa = CategorieJpa.builder()
                .uuid(uuidCategorie)
                .nom("POISSON")
                .description("Produits de la mer")
                .icone("fish-icon.svg")
                .build();
    }

    @Test
    void testSauvegarder_DevraitSauvegarderCategorie() {
        // Given
        when(mapper.versEntite(categorieModele)).thenReturn(categorieJpa);
        when(jpaRepository.save(categorieJpa)).thenReturn(categorieJpa);
        when(mapper.versModele(categorieJpa)).thenReturn(categorieModele);

        // When
        CategorieModele resultat = providerAdapter.sauvegarder(categorieModele);

        // Then
        assertNotNull(resultat);
        assertEquals("POISSON", resultat.getNom());
        verify(mapper, times(1)).versEntite(categorieModele);
        verify(jpaRepository, times(1)).save(categorieJpa);
        verify(mapper, times(1)).versModele(categorieJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerCategorie() {
        // Given
        when(jpaRepository.findById(uuidCategorie)).thenReturn(Optional.of(categorieJpa));
        when(mapper.versModele(categorieJpa)).thenReturn(categorieModele);

        // When
        Optional<CategorieModele> resultat = providerAdapter.trouverParUuid(uuidCategorie);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("POISSON", resultat.get().getNom());
        verify(jpaRepository, times(1)).findById(uuidCategorie);
        verify(mapper, times(1)).versModele(categorieJpa);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(jpaRepository.findById(uuidCategorie)).thenReturn(Optional.empty());

        // When
        Optional<CategorieModele> resultat = providerAdapter.trouverParUuid(uuidCategorie);

        // Then
        assertFalse(resultat.isPresent());
        verify(jpaRepository, times(1)).findById(uuidCategorie);
        verify(mapper, never()).versModele(any());
    }

    @Test
    void testTrouverParNom_DevraitRetournerCategorie() {
        // Given
        when(jpaRepository.findByNom("POISSON")).thenReturn(Optional.of(categorieJpa));
        when(mapper.versModele(categorieJpa)).thenReturn(categorieModele);

        // When
        Optional<CategorieModele> resultat = providerAdapter.trouverParNom("POISSON");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("POISSON", resultat.get().getNom());
        verify(jpaRepository, times(1)).findByNom("POISSON");
        verify(mapper, times(1)).versModele(categorieJpa);
    }

    @Test
    void testTrouverTous_DevraitRetournerListeCategories() {
        // Given
        CategorieJpa categorieJpa2 = CategorieJpa.builder()
                .uuid(UUID.randomUUID())
                .nom("VIANDE")
                .description("Produits carnés")
                .build();
        CategorieModele categorieModele2 = CategorieModele.builder()
                .uuid(categorieJpa2.getUuid())
                .nom("VIANDE")
                .description("Produits carnés")
                .build();

        when(jpaRepository.findAll()).thenReturn(Arrays.asList(categorieJpa, categorieJpa2));
        when(mapper.versModele(categorieJpa)).thenReturn(categorieModele);
        when(mapper.versModele(categorieJpa2)).thenReturn(categorieModele2);

        // When
        List<CategorieModele> resultat = providerAdapter.trouverTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(2)).versModele(any(CategorieJpa.class));
    }

    @Test
    void testMettreAJour_DevraitMettreAJourCategorie() {
        // Given
        CategorieModele categorieModifiee = CategorieModele.builder()
                .nom("POISSON_FRAIS")
                .description("Poissons frais du jour")
                .build();

        when(jpaRepository.findById(uuidCategorie)).thenReturn(Optional.of(categorieJpa));
        doNothing().when(mapper).mettreAJour(categorieJpa, categorieModifiee);
        when(jpaRepository.save(categorieJpa)).thenReturn(categorieJpa);
        when(mapper.versModele(categorieJpa)).thenReturn(categorieModifiee);

        // When
        CategorieModele resultat = providerAdapter.mettreAJour(uuidCategorie, categorieModifiee);

        // Then
        assertNotNull(resultat);
        verify(jpaRepository, times(1)).findById(uuidCategorie);
        verify(mapper, times(1)).mettreAJour(categorieJpa, categorieModifiee);
        verify(jpaRepository, times(1)).save(categorieJpa);
        verify(mapper, times(1)).versModele(categorieJpa);
    }

    @Test
    void testMettreAJour_DevraitLancerExceptionSiCategorieNonTrouvee() {
        // Given
        when(jpaRepository.findById(uuidCategorie)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class,
            () -> providerAdapter.mettreAJour(uuidCategorie, categorieModele));
        verify(jpaRepository, times(1)).findById(uuidCategorie);
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerCategorie() {
        // Given
        doNothing().when(jpaRepository).deleteById(uuidCategorie);

        // When
        providerAdapter.supprimerParUuid(uuidCategorie);

        // Then
        verify(jpaRepository, times(1)).deleteById(uuidCategorie);
    }

    @Test
    void testExisteParNom_DevraitRetournerTrue() {
        // Given
        when(jpaRepository.existsByNom("POISSON")).thenReturn(true);

        // When
        boolean resultat = providerAdapter.existeParNom("POISSON");

        // Then
        assertTrue(resultat);
        verify(jpaRepository, times(1)).existsByNom("POISSON");
    }

    @Test
    void testExisteParNom_DevraitRetournerFalse() {
        // Given
        when(jpaRepository.existsByNom("INEXISTANT")).thenReturn(false);

        // When
        boolean resultat = providerAdapter.existeParNom("INEXISTANT");

        // Then
        assertFalse(resultat);
        verify(jpaRepository, times(1)).existsByNom("INEXISTANT");
    }
}
