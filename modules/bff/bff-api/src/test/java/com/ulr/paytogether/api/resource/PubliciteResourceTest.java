package com.ulr.paytogether.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulr.paytogether.api.apiadapter.PubliciteApiAdapter;
import com.ulr.paytogether.api.dto.ImageDto;
import com.ulr.paytogether.api.dto.PubliciteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
 * Tests d'intégration pour PubliciteResource
 */
@WebMvcTest(PubliciteResource.class)
class PubliciteResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PubliciteApiAdapter publiciteApiAdapter;

    private PubliciteDTO publiciteDTO;
    private UUID uuidPublicite;

    @BeforeEach
    void setUp() {
        uuidPublicite = UUID.randomUUID();

        publiciteDTO = PubliciteDTO.builder()
                .uuid(uuidPublicite)
                .titre("Publicité de test")
                .description("Description de la publicité")
                .lienExterne("https://example.com")
                .listeImages(List.of(
                        new ImageDto(UUID.randomUUID(), "image1.jpg", null, null)
                ))
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
    }

    // ==================== Tests pour POST /api/publicites ====================

    @Test
    void testCreer_DevraitCreerPublicite() throws Exception {
        // Given
        when(publiciteApiAdapter.creer(any(PubliciteDTO.class))).thenReturn(publiciteDTO);

        // When & Then
        mockMvc.perform(post("/api/publicites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publiciteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(uuidPublicite.toString()))
                .andExpect(jsonPath("$.titre").value("Publicité de test"))
                .andExpect(jsonPath("$.active").value(true));

        verify(publiciteApiAdapter, times(1)).creer(any(PubliciteDTO.class));
    }

    @Test
    void testCreer_DevraitRetournerErreurSiTitreManquant() throws Exception {
        // Given
        PubliciteDTO dtoInvalide = PubliciteDTO.builder()
                .description("Description")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(30))
                .build();

        // When & Then
        mockMvc.perform(post("/api/publicites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(publiciteApiAdapter, never()).creer(any());
    }

    // ==================== Tests pour GET /api/publicites/{uuid} ====================

    @Test
    void testLireParUuid_DevraitRetournerPublicite() throws Exception {
        // Given
        when(publiciteApiAdapter.trouverParUuid(uuidPublicite)).thenReturn(Optional.of(publiciteDTO));

        // When & Then
        mockMvc.perform(get("/api/publicites/{uuid}", uuidPublicite))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuidPublicite.toString()))
                .andExpect(jsonPath("$.titre").value("Publicité de test"));

        verify(publiciteApiAdapter, times(1)).trouverParUuid(uuidPublicite);
    }

    @Test
    void testLireParUuid_DevraitRetourner404SiNonTrouve() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(publiciteApiAdapter.trouverParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/publicites/{uuid}", uuidInexistant))
                .andExpect(status().isNotFound());

        verify(publiciteApiAdapter, times(1)).trouverParUuid(uuidInexistant);
    }

    // ==================== Tests pour GET /api/publicites ====================

    @Test
    void testLireTous_DevraitRetournerToutesLesPublicites() throws Exception {
        // Given
        PubliciteDTO publicite2 = PubliciteDTO.builder()
                .uuid(UUID.randomUUID())
                .titre("Publicité 2")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(15))
                .active(false)
                .build();

        List<PubliciteDTO> publicites = Arrays.asList(publiciteDTO, publicite2);
        when(publiciteApiAdapter.trouverTous()).thenReturn(publicites);

        // When & Then
        mockMvc.perform(get("/api/publicites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titre").value("Publicité de test"))
                .andExpect(jsonPath("$[1].titre").value("Publicité 2"));

        verify(publiciteApiAdapter, times(1)).trouverTous();
    }

    @Test
    void testLireTous_DevraitRetournerListeVide() throws Exception {
        // Given
        when(publiciteApiAdapter.trouverTous()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/publicites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(publiciteApiAdapter, times(1)).trouverTous();
    }

    // ==================== Tests pour GET /api/publicites/actives ====================

    @Test
    void testLireActives_DevraitRetournerPublicitesActives() throws Exception {
        // Given
        List<PubliciteDTO> publicites = List.of(publiciteDTO);
        when(publiciteApiAdapter.trouverActives()).thenReturn(publicites);

        // When & Then
        mockMvc.perform(get("/api/publicites/actives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(publiciteApiAdapter, times(1)).trouverActives();
    }

    @Test
    void testLireActives_DevraitRetournerListeVide() throws Exception {
        // Given
        when(publiciteApiAdapter.trouverActives()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/publicites/actives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(publiciteApiAdapter, times(1)).trouverActives();
    }

    // ==================== Tests pour PUT /api/publicites/{uuid} ====================

    @Test
    void testMettreAJour_DevraitMettreAJourPublicite() throws Exception {
        // Given
        PubliciteDTO publiciteModifiee = PubliciteDTO.builder()
                .uuid(uuidPublicite)
                .titre("Titre modifié")
                .description("Description modifiée")
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(45))
                .active(false)
                .build();

        when(publiciteApiAdapter.mettreAJour(eq(uuidPublicite), any(PubliciteDTO.class)))
                .thenReturn(publiciteModifiee);

        // When & Then
        mockMvc.perform(put("/api/publicites/{uuid}", uuidPublicite)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publiciteModifiee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Titre modifié"))
                .andExpect(jsonPath("$.active").value(false));

        verify(publiciteApiAdapter, times(1)).mettreAJour(eq(uuidPublicite), any(PubliciteDTO.class));
    }

    // ==================== Tests pour DELETE /api/publicites/{uuid} ====================

    @Test
    void testSupprimer_DevraitSupprimerPublicite() throws Exception {
        // Given
        doNothing().when(publiciteApiAdapter).supprimerParUuid(uuidPublicite);

        // When & Then
        mockMvc.perform(delete("/api/publicites/{uuid}", uuidPublicite))
                .andExpect(status().isNoContent());

        verify(publiciteApiAdapter, times(1)).supprimerParUuid(uuidPublicite);
    }

    // ==================== Tests avec différents cas d'erreur ====================

    @Test
    void testCreer_AvecCorpsRequeteVide() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/publicites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLireParUuid_AvecUuidInvalide() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/publicites/{uuid}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreer_DevraitRetournerErreurSiDateDebutManquante() throws Exception {
        // Given
        PubliciteDTO dtoInvalide = PubliciteDTO.builder()
                .titre("Publicité")
                .description("Description")
                .dateFin(LocalDateTime.now().plusDays(30))
                .build();

        // When & Then
        mockMvc.perform(post("/api/publicites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(publiciteApiAdapter, never()).creer(any());
    }

    @Test
    void testCreer_DevraitRetournerErreurSiDateFinManquante() throws Exception {
        // Given
        PubliciteDTO dtoInvalide = PubliciteDTO.builder()
                .titre("Publicité")
                .description("Description")
                .dateDebut(LocalDateTime.now())
                .build();

        // When & Then
        mockMvc.perform(post("/api/publicites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(publiciteApiAdapter, never()).creer(any());
    }
}
