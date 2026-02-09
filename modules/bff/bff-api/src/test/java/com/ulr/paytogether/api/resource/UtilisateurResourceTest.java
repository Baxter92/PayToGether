package com.ulr.paytogether.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulr.paytogether.api.apiadapter.UtilisateurApiAdapter;
import com.ulr.paytogether.api.dto.CreerUtilisateurDTO;
import com.ulr.paytogether.api.dto.UtilisateurDTO;
import com.ulr.paytogether.core.enumeration.RoleUtilisateur;
import com.ulr.paytogether.core.enumeration.StatutUtilisateur;
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
 * Tests d'intégration pour UtilisateurResource
 */
@WebMvcTest(UtilisateurResource.class)
class UtilisateurResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtilisateurApiAdapter apiAdapter;

    private UtilisateurDTO utilisateurDTO;
    private CreerUtilisateurDTO creerUtilisateurDTO;
    private UUID uuidUtilisateur;

    @BeforeEach
    void setUp() {
        uuidUtilisateur = UUID.randomUUID();

        utilisateurDTO = UtilisateurDTO.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasseSecurise123")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        creerUtilisateurDTO = CreerUtilisateurDTO.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .motDePasse("motDePasseSecurise123")
                .build();
    }

    // ==================== Tests pour POST /api/utilisateurs ====================

    @Test
    void testCreer_DevraitCreerUtilisateur() throws Exception {
        // Given
        when(apiAdapter.creer(any(CreerUtilisateurDTO.class))).thenReturn(utilisateurDTO);

        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerUtilisateurDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(uuidUtilisateur.toString()))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.prenom").value("Jean"))
                .andExpect(jsonPath("$.email").value("jean.dupont@example.com"))
                .andExpect(jsonPath("$.role").value("UTILISATEUR"));

        verify(apiAdapter, times(1)).creer(any(CreerUtilisateurDTO.class));
    }

    @Test
    void testCreer_DevraitRetournerErreurSiNomManquant() throws Exception {
        // Given
        CreerUtilisateurDTO dtoInvalide = CreerUtilisateurDTO.builder()
                .prenom("Jean")
                .email("jean@example.com")
                .motDePasse("motDePasse123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(apiAdapter, never()).creer(any());
    }

    @Test
    void testCreer_DevraitRetournerErreurSiEmailInvalide() throws Exception {
        // Given
        CreerUtilisateurDTO dtoInvalide = CreerUtilisateurDTO.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("email-invalide")
                .motDePasse("motDePasse123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(apiAdapter, never()).creer(any());
    }

    @Test
    void testCreer_DevraitRetournerErreurSiMotDePasseManquant() throws Exception {
        // Given
        CreerUtilisateurDTO dtoInvalide = CreerUtilisateurDTO.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@example.com")
                .build();

        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalide)))
                .andExpect(status().isBadRequest());

        verify(apiAdapter, never()).creer(any());
    }

    // ==================== Tests pour GET /api/utilisateurs/{uuid} ====================

    @Test
    void testLireParUuid_DevraitRetournerUtilisateur() throws Exception {
        // Given
        when(apiAdapter.trouverParUuid(uuidUtilisateur)).thenReturn(Optional.of(utilisateurDTO));

        // When & Then
        mockMvc.perform(get("/api/utilisateurs/{uuid}", uuidUtilisateur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuidUtilisateur.toString()))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.email").value("jean.dupont@example.com"));

        verify(apiAdapter, times(1)).trouverParUuid(uuidUtilisateur);
    }

    @Test
    void testLireParUuid_DevraitRetourner404SiNonTrouve() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(apiAdapter.trouverParUuid(uuidInexistant)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/utilisateurs/{uuid}", uuidInexistant))
                .andExpect(status().isNotFound());

        verify(apiAdapter, times(1)).trouverParUuid(uuidInexistant);
    }

    // ==================== Tests pour GET /api/utilisateurs/email/{email} ====================

    @Test
    void testLireParEmail_DevraitRetournerUtilisateur() throws Exception {
        // Given
        String email = "jean.dupont@example.com";
        when(apiAdapter.trouverParEmail(email)).thenReturn(Optional.of(utilisateurDTO));

        // When & Then
        mockMvc.perform(get("/api/utilisateurs/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nom").value("Dupont"));

        verify(apiAdapter, times(1)).trouverParEmail(email);
    }

    @Test
    void testLireParEmail_DevraitRetourner404SiNonTrouve() throws Exception {
        // Given
        String emailInexistant = "inexistant@example.com";
        when(apiAdapter.trouverParEmail(emailInexistant)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/utilisateurs/email/{email}", emailInexistant))
                .andExpect(status().isNotFound());

        verify(apiAdapter, times(1)).trouverParEmail(emailInexistant);
    }

    // ==================== Tests pour GET /api/utilisateurs ====================

    @Test
    void testLireTous_DevraitRetournerTousLesUtilisateurs() throws Exception {
        // Given
        UtilisateurDTO utilisateur2 = UtilisateurDTO.builder()
                .uuid(UUID.randomUUID())
                .nom("Martin")
                .prenom("Marie")
                .email("marie.martin@example.com")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.VENDEUR)
                .build();

        List<UtilisateurDTO> utilisateurs = Arrays.asList(utilisateurDTO, utilisateur2);
        when(apiAdapter.trouverTous()).thenReturn(utilisateurs);

        // When & Then
        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nom").value("Dupont"))
                .andExpect(jsonPath("$[1].nom").value("Martin"));

        verify(apiAdapter, times(1)).trouverTous();
    }

    @Test
    void testLireTous_DevraitRetournerListeVide() throws Exception {
        // Given
        when(apiAdapter.trouverTous()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(apiAdapter, times(1)).trouverTous();
    }

    // ==================== Tests pour PUT /api/utilisateurs/{uuid} ====================

    @Test
    void testMettreAJour_DevraitMettreAJourUtilisateur() throws Exception {
        // Given
        UtilisateurDTO utilisateurMisAJour = UtilisateurDTO.builder()
                .uuid(uuidUtilisateur)
                .nom("Dupont")
                .prenom("Jean-Claude")
                .email("jean.dupont@example.com")
                .motDePasse("nouveauMotDePasse123")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.UTILISATEUR)
                .build();

        when(apiAdapter.mettreAJour(eq(uuidUtilisateur), any(UtilisateurDTO.class)))
                .thenReturn(utilisateurMisAJour);

        // When & Then
        mockMvc.perform(put("/api/utilisateurs/{uuid}", uuidUtilisateur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(utilisateurMisAJour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Jean-Claude"));

        verify(apiAdapter, times(1)).mettreAJour(eq(uuidUtilisateur), any(UtilisateurDTO.class));
    }

    @Test
    void testMettreAJour_DevraitRetourner404SiUtilisateurNonTrouve() throws Exception {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(apiAdapter.mettreAJour(eq(uuidInexistant), any(UtilisateurDTO.class)))
                .thenThrow(new RuntimeException("Utilisateur non trouvé"));

        // When & Then
        mockMvc.perform(put("/api/utilisateurs/{uuid}", uuidInexistant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(utilisateurDTO)))
                .andExpect(status().isNotFound());

        verify(apiAdapter, times(1)).mettreAJour(eq(uuidInexistant), any(UtilisateurDTO.class));
    }

    // ==================== Tests pour DELETE /api/utilisateurs/{uuid} ====================

    @Test
    void testSupprimer_DevraitSupprimerUtilisateur() throws Exception {
        // Given
        doNothing().when(apiAdapter).supprimer(uuidUtilisateur);

        // When & Then
        mockMvc.perform(delete("/api/utilisateurs/{uuid}", uuidUtilisateur))
                .andExpect(status().isNoContent());

        verify(apiAdapter, times(1)).supprimer(uuidUtilisateur);
    }

    @Test
    void testSupprimer_AvecUuidValide() throws Exception {
        // Given
        UUID uuidASupprimer = UUID.randomUUID();
        doNothing().when(apiAdapter).supprimer(uuidASupprimer);

        // When & Then
        mockMvc.perform(delete("/api/utilisateurs/{uuid}", uuidASupprimer))
                .andExpect(status().isNoContent());

        verify(apiAdapter, times(1)).supprimer(uuidASupprimer);
    }

    // ==================== Tests pour GET /api/utilisateurs/existe/{email} ====================

    @Test
    void testExisteParEmail_DevraitRetournerTrueQuandEmailExiste() throws Exception {
        // Given
        String email = "jean.dupont@example.com";
        when(apiAdapter.existeParEmail(email)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/utilisateurs/existe/{email}", email))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(apiAdapter, times(1)).existeParEmail(email);
    }

    @Test
    void testExisteParEmail_DevraitRetournerFalseQuandEmailNExistePas() throws Exception {
        // Given
        String emailInexistant = "inexistant@example.com";
        when(apiAdapter.existeParEmail(emailInexistant)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/utilisateurs/existe/{email}", emailInexistant))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(apiAdapter, times(1)).existeParEmail(emailInexistant);
    }

    // ==================== Tests avec différents rôles ====================

    @Test
    void testCreer_AvecRoleVendeur() throws Exception {
        // Given
        UtilisateurDTO vendeur = UtilisateurDTO.builder()
                .uuid(UUID.randomUUID())
                .nom("Commerçant")
                .prenom("Pierre")
                .email("pierre@commerce.com")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.VENDEUR)
                .build();

        when(apiAdapter.creer(any(CreerUtilisateurDTO.class))).thenReturn(vendeur);

        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerUtilisateurDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("VENDEUR"));

        verify(apiAdapter, times(1)).creer(any(CreerUtilisateurDTO.class));
    }

    @Test
    void testCreer_AvecRoleAdmin() throws Exception {
        // Given
        UtilisateurDTO admin = UtilisateurDTO.builder()
                .uuid(UUID.randomUUID())
                .nom("Admin")
                .prenom("Super")
                .email("admin@paytogether.com")
                .statut(StatutUtilisateur.ACTIF)
                .role(RoleUtilisateur.ADMIN)
                .build();

        when(apiAdapter.creer(any(CreerUtilisateurDTO.class))).thenReturn(admin);

        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerUtilisateurDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(apiAdapter, times(1)).creer(any(CreerUtilisateurDTO.class));
    }

    // ==================== Tests avec différents cas d'erreur ====================

    @Test
    void testCreer_AvecCorpsRequeteVide() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLireParUuid_AvecUuidInvalide() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/utilisateurs/{uuid}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreer_AvecEmailDuplique() throws Exception {
        // Given
        when(apiAdapter.creer(any(CreerUtilisateurDTO.class)))
                .thenThrow(new RuntimeException("Email déjà existant"));

        // When & Then
        mockMvc.perform(post("/api/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerUtilisateurDTO)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testMettreAJour_DevraitRetournerConflitSiConcurrence() throws Exception {
        // Given
        when(apiAdapter.mettreAJour(eq(uuidUtilisateur), any(UtilisateurDTO.class)))
                .thenThrow(new RuntimeException("Conflit de version"));

        // When & Then
        mockMvc.perform(put("/api/utilisateurs/{uuid}", uuidUtilisateur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(utilisateurDTO)))
                .andExpect(status().is5xxServerError());
    }
}
