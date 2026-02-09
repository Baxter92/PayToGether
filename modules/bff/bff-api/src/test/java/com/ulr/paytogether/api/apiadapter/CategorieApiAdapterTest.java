package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CategorieDTO;
import com.ulr.paytogether.api.mapper.CategorieMapper;
import com.ulr.paytogether.core.domaine.service.CategorieService;
import com.ulr.paytogether.core.modele.CategorieModele;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CategorieApiAdapter
 */
@ExtendWith(MockitoExtension.class)
class CategorieApiAdapterTest {

    @Mock
    private CategorieService categorieService;

    @Mock
    private CategorieMapper mapper;

    @InjectMocks
    private CategorieApiAdapter apiAdapter;

    private CategorieModele categorieModele;
    private CategorieDTO categorieDTO;
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

        categorieDTO = CategorieDTO.builder()
                .uuid(uuidCategorie)
                .nom("POISSON")
                .description("Produits de la mer")
                .icone("fish-icon.svg")
                .build();
    }

    @Test
    void testCreer_DevraitCreerCategorie() {
        // Given
        when(mapper.dtoVersModele(categorieDTO)).thenReturn(categorieModele);
        when(categorieService.creer(categorieModele)).thenReturn(categorieModele);
        when(mapper.modeleVersDto(categorieModele)).thenReturn(categorieDTO);

        // When
        CategorieDTO resultat = apiAdapter.creer(categorieDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("POISSON", resultat.getNom());
        verify(mapper, times(1)).dtoVersModele(categorieDTO);
        verify(categorieService, times(1)).creer(categorieModele);
        verify(mapper, times(1)).modeleVersDto(categorieModele);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerCategorie() {
        // Given
        when(categorieService.lireParUuid(uuidCategorie)).thenReturn(Optional.of(categorieModele));
        when(mapper.modeleVersDto(categorieModele)).thenReturn(categorieDTO);

        // When
        Optional<CategorieDTO> resultat = apiAdapter.trouverParUuid(uuidCategorie);

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("POISSON", resultat.get().getNom());
        verify(categorieService, times(1)).lireParUuid(uuidCategorie);
        verify(mapper, times(1)).modeleVersDto(categorieModele);
    }

    @Test
    void testTrouverParUuid_DevraitRetournerOptionalVide() {
        // Given
        when(categorieService.lireParUuid(uuidCategorie)).thenReturn(Optional.empty());

        // When
        Optional<CategorieDTO> resultat = apiAdapter.trouverParUuid(uuidCategorie);

        // Then
        assertFalse(resultat.isPresent());
        verify(categorieService, times(1)).lireParUuid(uuidCategorie);
        verify(mapper, never()).modeleVersDto(any());
    }

    @Test
    void testTrouverParNom_DevraitRetournerCategorie() {
        // Given
        when(categorieService.lireParNom("POISSON")).thenReturn(Optional.of(categorieModele));
        when(mapper.modeleVersDto(categorieModele)).thenReturn(categorieDTO);

        // When
        Optional<CategorieDTO> resultat = apiAdapter.trouverParNom("POISSON");

        // Then
        assertTrue(resultat.isPresent());
        assertEquals("POISSON", resultat.get().getNom());
        verify(categorieService, times(1)).lireParNom("POISSON");
        verify(mapper, times(1)).modeleVersDto(categorieModele);
    }

    @Test
    void testTrouverTous_DevraitRetournerListeCategories() {
        // Given
        CategorieModele categorieModele2 = CategorieModele.builder()
                .uuid(UUID.randomUUID())
                .nom("VIANDE")
                .description("Produits carnés")
                .build();
        CategorieDTO categorieDTO2 = CategorieDTO.builder()
                .uuid(categorieModele2.getUuid())
                .nom("VIANDE")
                .description("Produits carnés")
                .build();

        when(categorieService.lireTous()).thenReturn(Arrays.asList(categorieModele, categorieModele2));
        when(mapper.modeleVersDto(categorieModele)).thenReturn(categorieDTO);
        when(mapper.modeleVersDto(categorieModele2)).thenReturn(categorieDTO2);

        // When
        List<CategorieDTO> resultat = apiAdapter.trouverTous();

        // Then
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(categorieService, times(1)).lireTous();
        verify(mapper, times(2)).modeleVersDto(any(CategorieModele.class));
    }

    @Test
    void testMettreAJour_DevraitMettreAJourCategorie() {
        // Given
        CategorieDTO categorieModifieeDTO = CategorieDTO.builder()
                .uuid(uuidCategorie)
                .nom("POISSON_FRAIS")
                .description("Poissons frais du jour")
                .build();
        CategorieModele categorieModifieeModele = CategorieModele.builder()
                .uuid(uuidCategorie)
                .nom("POISSON_FRAIS")
                .description("Poissons frais du jour")
                .build();

        when(mapper.dtoVersModele(categorieModifieeDTO)).thenReturn(categorieModifieeModele);
        when(categorieService.mettreAJour(eq(uuidCategorie), any(CategorieModele.class)))
                .thenReturn(categorieModifieeModele);
        when(mapper.modeleVersDto(categorieModifieeModele)).thenReturn(categorieModifieeDTO);

        // When
        CategorieDTO resultat = apiAdapter.mettreAJour(uuidCategorie, categorieModifieeDTO);

        // Then
        assertNotNull(resultat);
        assertEquals("POISSON_FRAIS", resultat.getNom());
        verify(mapper, times(1)).dtoVersModele(categorieModifieeDTO);
        verify(categorieService, times(1)).mettreAJour(eq(uuidCategorie), any(CategorieModele.class));
        verify(mapper, times(1)).modeleVersDto(categorieModifieeModele);
    }

    @Test
    void testSupprimerParUuid_DevraitSupprimerCategorie() {
        // Given
        doNothing().when(categorieService).supprimerParUuid(uuidCategorie);

        // When
        apiAdapter.supprimerParUuid(uuidCategorie);

        // Then
        verify(categorieService, times(1)).supprimerParUuid(uuidCategorie);
    }

    @Test
    void testExisteParNom_DevraitRetournerTrue() {
        // Given
        when(categorieService.existeParNom("POISSON")).thenReturn(true);

        // When
        boolean resultat = apiAdapter.existeParNom("POISSON");

        // Then
        assertTrue(resultat);
        verify(categorieService, times(1)).existeParNom("POISSON");
    }

    @Test
    void testExisteParNom_DevraitRetournerFalse() {
        // Given
        when(categorieService.existeParNom("INEXISTANT")).thenReturn(false);

        // When
        boolean resultat = apiAdapter.existeParNom("INEXISTANT");

        // Then
        assertFalse(resultat);
        verify(categorieService, times(1)).existeParNom("INEXISTANT");
    }
}
