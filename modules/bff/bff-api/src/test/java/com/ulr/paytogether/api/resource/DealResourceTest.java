package com.ulr.paytogether.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulr.paytogether.api.apiadapter.DealApiAdapter;
import com.ulr.paytogether.api.dto.DealDTO;
import com.ulr.paytogether.api.dto.DealResponseDto;
import com.ulr.paytogether.core.enumeration.StatutDeal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
 * Tests d'intégration pour DealResource
 */
@WebMvcTest(DealResource.class)
class DealResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DealApiAdapter dealApiAdapter;

    private DealDTO dealDTO;
    private DealResponseDto dealResponseDto;
    private UUID uuidDeal;
    private UUID uuidCreateur;
    private UUID uuidCategorie;

    @BeforeEach
    void setUp() {
        uuidDeal = UUID.randomUUID();
        uuidCreateur = UUID.randomUUID();
        uuidCategorie = UUID.randomUUID();

        dealDTO = DealDTO.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium")
                .description("Viande de qualité supérieure")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(7))
                .statut(StatutDeal.PUBLIE)
                .createurUuid(uuidCreateur)
                .categorieUuid(uuidCategorie)
                .listeImages(List.of("image1.jpg"))
                .ville("Montreal")
                .build();

        dealResponseDto = DealResponseDto.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium")
                .description("Viande de qualité supérieure")
                .prixDeal(new BigDecimal("150.00"))
                .prixPart(new BigDecimal("30.00"))
                .nbParticipants(5)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(7))
                .statut(StatutDeal.PUBLIE)
                .createurNom("Dupont Jean")
                .categorieNom("Viandes")
                .listeImages(List.of())
                .ville("Montreal")
                .build();
    }

    // ==================== Tests pour POST /api/deals ====================

    @Test
    void testCreer_DevraitCreerDeal() throws Exception {
        // Given
        when(dealApiAdapter.creerDeal(any(DealDTO.class))).thenReturn(dealResponseDto);

        // When & Then
        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(uuidDeal.toString()))
                .andExpect(jsonPath("$.titre").value("Filet de boeuf premium"))
                .andExpect(jsonPath("$.prixDeal").value(150.00))
                .andExpect(jsonPath("$.prixPart").value(30.00))
                .andExpect(jsonPath("$.nbParticipants").value(5))
                .andExpect(jsonPath("$.statut").value("PUBLIE"))
                .andExpect(jsonPath("$.ville").value("Montreal"));

        verify(dealApiAdapter, times(1)).creerDeal(any(DealDTO.class));
    }

    @Test
    void testCreer_DevraitRetournerErreurSiValidationEchoue() throws Exception {
        // Given
        when(dealApiAdapter.creerDeal(any(DealDTO.class)))
                .thenThrow(new IllegalArgumentException("L'attribut titre est obligatoire"));

        // When & Then
        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests pour GET /api/deals/{uuid} ====================

    @Test
    void testLireParUuid_DevraitRetournerDeal() throws Exception {
        // Given
        when(dealApiAdapter.lireParUuid(uuidDeal)).thenReturn(Optional.of(dealResponseDto));

        // When & Then
        mockMvc.perform(get("/api/deals/{uuid}", uuidDeal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuidDeal.toString()))
                .andExpect(jsonPath("$.titre").value("Filet de boeuf premium"))
                .andExpect(jsonPath("$.createurNom").value("Dupont Jean"))
                .andExpect(jsonPath("$.categorieNom").value("Viandes"));

        verify(dealApiAdapter, times(1)).lireParUuid(uuidDeal);
    }

    @Test
    void testLireParUuid_DevraitRetourner404SiNonTrouve() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(dealApiAdapter.lireParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/deals/{uuid}", uuidInexistant))
                .andExpect(status().isNotFound());

        verify(dealApiAdapter, times(1)).lireParUuid(uuidInexistant);
    }

    // ==================== Tests pour GET /api/deals ====================

    @Test
    void testLireTous_DevraitRetournerTousLesDeals() throws Exception {
        // Given
        DealResponseDto deal2 = DealResponseDto.builder()
                .uuid(UUID.randomUUID())
                .titre("Saumon frais")
                .description("Poisson de qualité")
                .prixDeal(new BigDecimal("80.00"))
                .prixPart(new BigDecimal("20.00"))
                .nbParticipants(4)
                .statut(StatutDeal.PUBLIE)
                .ville("Quebec")
                .build();

        List<DealResponseDto> deals = Arrays.asList(dealResponseDto, deal2);
        when(dealApiAdapter.lireTous()).thenReturn(deals);

        // When & Then
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titre").value("Filet de boeuf premium"))
                .andExpect(jsonPath("$[1].titre").value("Saumon frais"));

        verify(dealApiAdapter, times(1)).lireTous();
    }

    @Test
    void testLireTous_DevraitRetournerListeVide() throws Exception {
        // Given
        when(dealApiAdapter.lireTous()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(dealApiAdapter, times(1)).lireTous();
    }

    // ==================== Tests pour GET /api/deals/statut/{statut} ====================

    @Test
    void testLireParStatut_DevraitRetournerDealsPublies() throws Exception {
        // Given
        List<DealResponseDto> dealsPublies = List.of(dealResponseDto);
        when(dealApiAdapter.lireTousByStatut(StatutDeal.PUBLIE)).thenReturn(dealsPublies);

        // When & Then
        mockMvc.perform(get("/api/deals/statut/{statut}", "PUBLIE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].statut").value("PUBLIE"));

        verify(dealApiAdapter, times(1)).lireTousByStatut(StatutDeal.PUBLIE);
    }

    @Test
    void testLireParStatut_DevraitRetournerDealsBrouillon() throws Exception {
        // Given
        DealResponseDto dealBrouillon = DealResponseDto.builder()
                .uuid(UUID.randomUUID())
                .titre("Deal en brouillon")
                .prixDeal(new BigDecimal("50.00"))
                .prixPart(new BigDecimal("10.00"))
                .nbParticipants(5)
                .statut(StatutDeal.BROUILLON)
                .ville("Montreal")
                .build();

        when(dealApiAdapter.lireTousByStatut(StatutDeal.BROUILLON)).thenReturn(List.of(dealBrouillon));

        // When & Then
        mockMvc.perform(get("/api/deals/statut/{statut}", "BROUILLON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].statut").value("BROUILLON"));

        verify(dealApiAdapter, times(1)).lireTousByStatut(StatutDeal.BROUILLON);
    }

    // ==================== Tests pour GET /api/deals/createur/{createurUuid} ====================

    @Test
    void testLireParCreateur_DevraitRetournerDealsCreateur() throws Exception {
        // Given
        List<DealResponseDto> deals = List.of(dealResponseDto);
        when(dealApiAdapter.lireTousByCreateurUuid(uuidCreateur)).thenReturn(deals);

        // When & Then
        mockMvc.perform(get("/api/deals/createur/{createurUuid}", uuidCreateur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre").value("Filet de boeuf premium"));

        verify(dealApiAdapter, times(1)).lireTousByCreateurUuid(uuidCreateur);
    }

    @Test
    void testLireParCreateur_DevraitRetournerListeVideSiAucunDeal() throws Exception {
        // Given
        UUID createurSansDeals = UUID.randomUUID();
        when(dealApiAdapter.lireTousByCreateurUuid(createurSansDeals)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/deals/createur/{createurUuid}", createurSansDeals))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(dealApiAdapter, times(1)).lireTousByCreateurUuid(createurSansDeals);
    }

    // ==================== Tests pour GET /api/deals/categorie/{categorieUuid} ====================

    @Test
    void testLireParCategorie_DevraitRetournerDealsCategorie() throws Exception {
        // Given
        List<DealResponseDto> deals = List.of(dealResponseDto);
        when(dealApiAdapter.lireTousByCategorieUuid(uuidCategorie)).thenReturn(deals);

        // When & Then
        mockMvc.perform(get("/api/deals/categorie/{categorieUuid}", uuidCategorie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre").value("Filet de boeuf premium"));

        verify(dealApiAdapter, times(1)).lireTousByCategorieUuid(uuidCategorie);
    }

    @Test
    void testLireParCategorie_DevraitRetournerListeVideSiAucunDeal() throws Exception {
        // Given
        UUID categorieSansDeals = UUID.randomUUID();
        when(dealApiAdapter.lireTousByCategorieUuid(categorieSansDeals)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/deals/categorie/{categorieUuid}", categorieSansDeals))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(dealApiAdapter, times(1)).lireTousByCategorieUuid(categorieSansDeals);
    }

    // ==================== Tests pour PUT /api/deals/{uuid} ====================

    @Test
    void testMettreAJour_DevraitMettreAJourDeal() throws Exception {
        // Given
        DealResponseDto dealMisAJour = DealResponseDto.builder()
                .uuid(uuidDeal)
                .titre("Filet de boeuf premium - Prix réduit")
                .description("Viande de qualité supérieure - Offre spéciale")
                .prixDeal(new BigDecimal("120.00"))
                .prixPart(new BigDecimal("24.00"))
                .nbParticipants(5)
                .statut(StatutDeal.PUBLIE)
                .ville("Montreal")
                .build();

        when(dealApiAdapter.mettreAJour(eq(uuidDeal), any(DealDTO.class))).thenReturn(dealMisAJour);

        // When & Then
        mockMvc.perform(put("/api/deals/{uuid}", uuidDeal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Filet de boeuf premium - Prix réduit"))
                .andExpect(jsonPath("$.prixDeal").value(120.00));

        verify(dealApiAdapter, times(1)).mettreAJour(eq(uuidDeal), any(DealDTO.class));
    }

    @Test
    void testMettreAJour_DevraitRetournerErreurSiValidationEchoue() throws Exception {
        // Given
        when(dealApiAdapter.mettreAJour(eq(uuidDeal), any(DealDTO.class)))
                .thenThrow(new IllegalArgumentException("L'attribut ville est obligatoire"));

        // When & Then
        mockMvc.perform(put("/api/deals/{uuid}", uuidDeal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealDTO)))
                .andExpect(status().isBadRequest());

        verify(dealApiAdapter, times(1)).mettreAJour(eq(uuidDeal), any(DealDTO.class));
    }

    // ==================== Tests pour DELETE /api/deals/{uuid} ====================

    @Test
    void testSupprimer_DevraitSupprimerDeal() throws Exception {
        // Given
        when(dealApiAdapter.lireParUuid(uuidDeal)).thenReturn(Optional.of(dealResponseDto));
        doNothing().when(dealApiAdapter).supprimerParUuid(uuidDeal);

        // When & Then
        mockMvc.perform(delete("/api/deals/{uuid}", uuidDeal))
                .andExpect(status().isNoContent());

        verify(dealApiAdapter, times(1)).lireParUuid(uuidDeal);
        verify(dealApiAdapter, times(1)).supprimerParUuid(uuidDeal);
    }

    @Test
    void testSupprimer_DevraitRetourner404SiDealNonTrouve() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(dealApiAdapter.lireParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/deals/{uuid}", uuidInexistant))
                .andExpect(status().isNotFound());

        verify(dealApiAdapter, times(1)).lireParUuid(uuidInexistant);
        verify(dealApiAdapter, never()).supprimerParUuid(any());
    }

    // ==================== Tests avec différents cas d'erreur ====================

    @Test
    void testCreer_AvecCorpsRequeteVide() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/deals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLireParUuid_AvecUuidInvalide() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/deals/{uuid}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMettreAJour_DevraitRetournerConflitSiConcurrence() throws Exception {
        // Given
        when(dealApiAdapter.mettreAJour(eq(uuidDeal), any(DealDTO.class)))
                .thenThrow(new RuntimeException("Conflit de version"));

        // When & Then
        mockMvc.perform(put("/api/deals/{uuid}", uuidDeal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealDTO)))
                .andExpect(status().is5xxServerError());
    }
}
