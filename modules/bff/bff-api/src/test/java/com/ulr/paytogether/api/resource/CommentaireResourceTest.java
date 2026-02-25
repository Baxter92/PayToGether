package com.ulr.paytogether.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulr.paytogether.api.apiadapter.CommentaireApiAdapter;
import com.ulr.paytogether.api.dto.CommentaireDTO;
import com.ulr.paytogether.core.exception.ResourceNotFoundException;
import com.ulr.paytogether.core.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour CommentaireResource
 */
@WebMvcTest(CommentaireResource.class)
class CommentaireResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentaireApiAdapter apiAdapter;

    private CommentaireDTO commentaireDTO;
    private UUID commentaireUuid;
    private UUID dealUuid;
    private UUID utilisateurUuid;

    @BeforeEach
    void setUp() {
        commentaireUuid = UUID.randomUUID();
        dealUuid = UUID.randomUUID();
        utilisateurUuid = UUID.randomUUID();

        commentaireDTO = CommentaireDTO.builder()
                .uuid(commentaireUuid)
                .contenu("Excellent deal ! Je recommande.")
                .note(5)
                .utilisateurUuid(utilisateurUuid)
                .dealUuid(dealUuid)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();
    }

    @Test
    void creer_DevraitRetourner201_QuandCommentaireValide() throws Exception {
        // Given
        when(apiAdapter.creer(any(CommentaireDTO.class))).thenReturn(commentaireDTO);

        // When & Then
        mockMvc.perform(post("/api/commentaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentaireDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid", is(commentaireUuid.toString())))
                .andExpect(jsonPath("$.contenu", is("Excellent deal ! Je recommande.")))
                .andExpect(jsonPath("$.note", is(5)))
                .andExpect(jsonPath("$.utilisateurUuid", is(utilisateurUuid.toString())))
                .andExpect(jsonPath("$.dealUuid", is(dealUuid.toString())));

        verify(apiAdapter).creer(any(CommentaireDTO.class));
    }

    @Test
    void creer_DevraitRetourner400_QuandContenuVide() throws Exception {
        // Given
        CommentaireDTO dtoInvalide = CommentaireDTO.builder()
                .contenu("")
                .note(5)
                .utilisateurUuid(utilisateurUuid)
                .dealUuid(dealUuid)
                .build();

        // When & Then
        mockMvc.perform(post("/api/commentaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(apiAdapter, never()).creer(any());
    }

    @Test
    void creer_DevraitRetourner400_QuandNoteInvalide() throws Exception {
        // Given
        CommentaireDTO dtoInvalide = CommentaireDTO.builder()
                .contenu("Commentaire")
                .note(6) // Note invalide (max 5)
                .utilisateurUuid(utilisateurUuid)
                .dealUuid(dealUuid)
                .build();

        // When & Then
        mockMvc.perform(post("/api/commentaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(apiAdapter, never()).creer(any());
    }

    @Test
    void creer_DevraitRetourner400_QuandValidationException() throws Exception {
        // Given
        when(apiAdapter.creer(any(CommentaireDTO.class)))
                .thenThrow(new ValidationException("commentaire.contenu.obligatoire"));

        // When & Then
        mockMvc.perform(post("/api/commentaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentaireDTO)))
                .andExpect(status().isBadRequest());

        verify(apiAdapter).creer(any(CommentaireDTO.class));
    }

    @Test
    void trouverParUuid_DevraitRetourner200_QuandCommentaireExiste() throws Exception {
        // Given
        when(apiAdapter.trouverParUuid(commentaireUuid))
                .thenReturn(Optional.of(commentaireDTO));

        // When & Then
        mockMvc.perform(get("/api/commentaires/{uuid}", commentaireUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(commentaireUuid.toString())))
                .andExpect(jsonPath("$.contenu", is("Excellent deal ! Je recommande.")))
                .andExpect(jsonPath("$.note", is(5)));

        verify(apiAdapter).trouverParUuid(commentaireUuid);
    }

    @Test
    void trouverParUuid_DevraitRetourner404_QuandCommentaireNonExiste() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(apiAdapter.trouverParUuid(uuidInexistant))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/commentaires/{uuid}", uuidInexistant))
                .andExpect(status().isNotFound());

        verify(apiAdapter).trouverParUuid(uuidInexistant);
    }

    @Test
    void trouverParDeal_DevraitRetourner200AvecListeCommentaires() throws Exception {
        // Given
        CommentaireDTO commentaire2 = CommentaireDTO.builder()
                .uuid(UUID.randomUUID())
                .contenu("Bon deal")
                .note(4)
                .build();

        List<CommentaireDTO> commentaires = List.of(commentaireDTO, commentaire2);
        when(apiAdapter.trouverParDeal(dealUuid)).thenReturn(commentaires);

        // When & Then
        mockMvc.perform(get("/api/commentaires/deal/{dealUuid}", dealUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].contenu", is("Excellent deal ! Je recommande.")))
                .andExpect(jsonPath("$[1].contenu", is("Bon deal")));

        verify(apiAdapter).trouverParDeal(dealUuid);
    }

    @Test
    void trouverParDeal_DevraitRetourner200AvecListeVide_QuandAucunCommentaire() throws Exception {
        // Given
        when(apiAdapter.trouverParDeal(dealUuid)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/commentaires/deal/{dealUuid}", dealUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(apiAdapter).trouverParDeal(dealUuid);
    }

    @Test
    void trouverParDeal_DevraitRetourner404_QuandDealNonExiste() throws Exception {
        // Given
        UUID dealInexistant = UUID.randomUUID();
        when(apiAdapter.trouverParDeal(dealInexistant))
                .thenThrow(ResourceNotFoundException.parUuid("deal", dealInexistant));

        // When & Then
        mockMvc.perform(get("/api/commentaires/deal/{dealUuid}", dealInexistant))
                .andExpect(status().isNotFound());

        verify(apiAdapter).trouverParDeal(dealInexistant);
    }

    @Test
    void mettreAJour_DevraitRetourner200_QuandCommentaireValide() throws Exception {
        // Given
        CommentaireDTO dtoMisAJour = CommentaireDTO.builder()
                .uuid(commentaireUuid)
                .contenu("Commentaire mis à jour")
                .note(4)
                .utilisateurUuid(utilisateurUuid)
                .dealUuid(dealUuid)
                .build();

        when(apiAdapter.mettreAJour(eq(commentaireUuid), any(CommentaireDTO.class)))
                .thenReturn(dtoMisAJour);

        // When & Then
        mockMvc.perform(put("/api/commentaires/{uuid}", commentaireUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoMisAJour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", is(commentaireUuid.toString())))
                .andExpect(jsonPath("$.contenu", is("Commentaire mis à jour")))
                .andExpect(jsonPath("$.note", is(4)));

        verify(apiAdapter).mettreAJour(eq(commentaireUuid), any(CommentaireDTO.class));
    }

    @Test
    void mettreAJour_DevraitRetourner400_QuandDonneesInvalides() throws Exception {
        // Given
        CommentaireDTO dtoInvalide = CommentaireDTO.builder()
                .contenu("") // Contenu vide
                .note(5)
                .build();

        // When & Then
        mockMvc.perform(put("/api/commentaires/{uuid}", commentaireUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(apiAdapter, never()).mettreAJour(any(), any());
    }

    @Test
    void mettreAJour_DevraitRetourner404_QuandCommentaireNonExiste() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(apiAdapter.mettreAJour(eq(uuidInexistant), any(CommentaireDTO.class)))
                .thenThrow(ResourceNotFoundException.parUuid("commentaire", uuidInexistant));

        // When & Then
        mockMvc.perform(put("/api/commentaires/{uuid}", uuidInexistant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentaireDTO)))
                .andExpect(status().isNotFound());

        verify(apiAdapter).mettreAJour(eq(uuidInexistant), any(CommentaireDTO.class));
    }

    @Test
    void supprimer_DevraitRetourner204_QuandCommentaireExiste() throws Exception {
        // Given
        doNothing().when(apiAdapter).supprimer(commentaireUuid);

        // When & Then
        mockMvc.perform(delete("/api/commentaires/{uuid}", commentaireUuid))
                .andExpect(status().isNoContent());

        verify(apiAdapter).supprimer(commentaireUuid);
    }

    @Test
    void supprimer_DevraitRetourner404_QuandCommentaireNonExiste() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        doThrow(ResourceNotFoundException.parUuid("commentaire", uuidInexistant))
                .when(apiAdapter).supprimer(uuidInexistant);

        // When & Then
        mockMvc.perform(delete("/api/commentaires/{uuid}", uuidInexistant))
                .andExpect(status().isNotFound());

        verify(apiAdapter).supprimer(uuidInexistant);
    }
}

