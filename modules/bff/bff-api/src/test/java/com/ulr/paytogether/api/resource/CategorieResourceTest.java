package com.ulr.paytogether.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulr.paytogether.api.apiadapter.CategorieApiAdapter;
import com.ulr.paytogether.api.dto.CategorieDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour CategorieResource
 */
@WebMvcTest(CategorieResource.class)
class CategorieResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategorieApiAdapter categorieApiAdapter;

    private CategorieDTO categorieDTO;
    private UUID uuidCategorie;

    @BeforeEach
    void setUp() {
        uuidCategorie = UUID.randomUUID();

        categorieDTO = CategorieDTO.builder()
                .uuid(uuidCategorie)
                .nom("POISSON")
                .description("Produits de la mer")
                .icone("fish-icon.svg")
                .build();
    }

    // ==================== Tests pour POST /api/categories ====================

    @Test
    void testCreer_DevraitCreerCategorie() throws Exception {
        // Given
        when(categorieApiAdapter.creer(any(CategorieDTO.class))).thenReturn(categorieDTO);

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorieDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(uuidCategorie.toString()))
                .andExpect(jsonPath("$.nom").value("POISSON"))
                .andExpect(jsonPath("$.description").value("Produits de la mer"));

        verify(categorieApiAdapter, times(1)).creer(any(CategorieDTO.class));
    }

    @Test
    void testCreer_DevraitRetournerErreurSiNomManquant() throws Exception {
        // Given
        CategorieDTO dtoInvalide = CategorieDTO.builder()
                .description("Description")
                .build();

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(categorieApiAdapter, never()).creer(any());
    }

    // ==================== Tests pour GET /api/categories/{uuid} ====================

    @Test
    void testLireParUuid_DevraitRetournerCategorie() throws Exception {
        // Given
        when(categorieApiAdapter.trouverParUuid(uuidCategorie)).thenReturn(Optional.of(categorieDTO));

        // When & Then
        mockMvc.perform(get("/api/categories/{uuid}", uuidCategorie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuidCategorie.toString()))
                .andExpect(jsonPath("$.nom").value("POISSON"));

        verify(categorieApiAdapter, times(1)).trouverParUuid(uuidCategorie);
    }

    @Test
    void testLireParUuid_DevraitRetourner404SiNonTrouve() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(categorieApiAdapter.trouverParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/categories/{uuid}", uuidInexistant))
                .andExpect(status().isNotFound());

        verify(categorieApiAdapter, times(1)).trouverParUuid(uuidInexistant);
    }

    // ==================== Tests pour GET /api/categories/nom/{nom} ====================

    @Test
    void testLireParNom_DevraitRetournerCategorie() throws Exception {
        // Given
        when(categorieApiAdapter.trouverParNom("POISSON")).thenReturn(Optional.of(categorieDTO));

        // When & Then
        mockMvc.perform(get("/api/categories/nom/{nom}", "POISSON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("POISSON"));

        verify(categorieApiAdapter, times(1)).trouverParNom("POISSON");
    }

    @Test
    void testLireParNom_DevraitRetourner404SiNonTrouve() throws Exception {
        // Given
        when(categorieApiAdapter.trouverParNom("INEXISTANT")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/categories/nom/{nom}", "INEXISTANT"))
                .andExpect(status().isNotFound());

        verify(categorieApiAdapter, times(1)).trouverParNom("INEXISTANT");
    }

    // ==================== Tests pour GET /api/categories ====================

    @Test
    void testLireTous_DevraitRetournerToutesLesCategories() throws Exception {
        // Given
        CategorieDTO categorie2 = CategorieDTO.builder()
                .uuid(UUID.randomUUID())
                .nom("VIANDE")
                .description("Produits carnés")
                .build();

        List<CategorieDTO> categories = Arrays.asList(categorieDTO, categorie2);
        when(categorieApiAdapter.trouverTous()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nom").value("POISSON"))
                .andExpect(jsonPath("$[1].nom").value("VIANDE"));

        verify(categorieApiAdapter, times(1)).trouverTous();
    }

    @Test
    void testLireTous_DevraitRetournerListeVide() throws Exception {
        // Given
        when(categorieApiAdapter.trouverTous()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(categorieApiAdapter, times(1)).trouverTous();
    }

    // ==================== Tests pour GET /api/categories/existe/{nom} ====================

    @Test
    void testExisteParNom_DevraitRetournerTrue() throws Exception {
        // Given
        when(categorieApiAdapter.existeParNom("POISSON")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/categories/existe/{nom}", "POISSON"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(categorieApiAdapter, times(1)).existeParNom("POISSON");
    }

    @Test
    void testExisteParNom_DevraitRetournerFalse() throws Exception {
        // Given
        when(categorieApiAdapter.existeParNom("INEXISTANT")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/categories/existe/{nom}", "INEXISTANT"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(categorieApiAdapter, times(1)).existeParNom("INEXISTANT");
    }

    // ==================== Tests pour PUT /api/categories/{uuid} ====================

    @Test
    void testMettreAJour_DevraitMettreAJourCategorie() throws Exception {
        // Given
        CategorieDTO categorieModifiee = CategorieDTO.builder()
                .uuid(uuidCategorie)
                .nom("POISSON_FRAIS")
                .description("Poissons frais du jour")
                .icone("fresh-fish-icon.svg")
                .build();

        when(categorieApiAdapter.mettreAJour(eq(uuidCategorie), any(CategorieDTO.class)))
                .thenReturn(categorieModifiee);

        // When & Then
        mockMvc.perform(put("/api/categories/{uuid}", uuidCategorie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorieModifiee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("POISSON_FRAIS"))
                .andExpect(jsonPath("$.description").value("Poissons frais du jour"));

        verify(categorieApiAdapter, times(1)).mettreAJour(eq(uuidCategorie), any(CategorieDTO.class));
    }

    // ==================== Tests pour DELETE /api/categories/{uuid} ====================

    @Test
    void testSupprimer_DevraitSupprimerCategorie() throws Exception {
        // Given
        doNothing().when(categorieApiAdapter).supprimerParUuid(uuidCategorie);

        // When & Then
        mockMvc.perform(delete("/api/categories/{uuid}", uuidCategorie))
                .andExpect(status().isNoContent());

        verify(categorieApiAdapter, times(1)).supprimerParUuid(uuidCategorie);
    }

    // ==================== Tests avec différents cas d'erreur ====================

    @Test
    void testCreer_AvecCorpsRequeteVide() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLireParUuid_AvecUuidInvalide() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/{uuid}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreer_AvecNomTropLong() throws Exception {
        // Given
        String nomTropLong = "A".repeat(101);
        CategorieDTO dtoInvalide = CategorieDTO.builder()
                .nom(nomTropLong)
                .description("Description")
                .build();

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(categorieApiAdapter, never()).creer(any());
    }
}
