package com.ulr.paytogether.api.apiadapter;

import com.ulr.paytogether.api.dto.CommentaireDTO;
import com.ulr.paytogether.api.mapper.CommentaireMapper;
import com.ulr.paytogether.core.domaine.service.CommentaireService;
import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CommentaireApiAdapter
 */
@ExtendWith(MockitoExtension.class)
class CommentaireApiAdapterTest {

    @Mock
    private CommentaireService commentaireService;

    @Mock
    private CommentaireMapper mapper;

    @InjectMocks
    private CommentaireApiAdapter apiAdapter;

    private CommentaireDTO commentaireDTO;
    private CommentaireModele commentaireModele;
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
                .contenu("Excellent deal")
                .note(5)
                .utilisateurUuid(utilisateurUuid)
                .dealUuid(dealUuid)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        commentaireModele = CommentaireModele.builder()
                .uuid(commentaireUuid)
                .contenu("Excellent deal")
                .note(5)
                .utilisateur(UtilisateurModele.builder().uuid(utilisateurUuid).build())
                .deal(DealModele.builder().uuid(dealUuid).build())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();
    }

    @Test
    void creer_DevraitCreerCommentaire() {
        // Given
        when(mapper.dtoVersModele(commentaireDTO)).thenReturn(commentaireModele);
        when(commentaireService.creer(commentaireModele)).thenReturn(commentaireModele);
        when(mapper.modeleVersDto(commentaireModele)).thenReturn(commentaireDTO);

        // When
        CommentaireDTO resultat = apiAdapter.creer(commentaireDTO);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(commentaireUuid);
        assertThat(resultat.getContenu()).isEqualTo("Excellent deal");
        assertThat(resultat.getNote()).isEqualTo(5);

        verify(mapper).dtoVersModele(commentaireDTO);
        verify(commentaireService).creer(commentaireModele);
        verify(mapper).modeleVersDto(commentaireModele);
    }

    @Test
    void trouverParUuid_DevraitRetournerCommentaire_QuandExiste() {
        // Given
        when(commentaireService.lireParUuid(commentaireUuid))
                .thenReturn(Optional.of(commentaireModele));
        when(mapper.modeleVersDto(commentaireModele)).thenReturn(commentaireDTO);

        // When
        Optional<CommentaireDTO> resultat = apiAdapter.trouverParUuid(commentaireUuid);

        // Then
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getUuid()).isEqualTo(commentaireUuid);
        assertThat(resultat.get().getContenu()).isEqualTo("Excellent deal");

        verify(commentaireService).lireParUuid(commentaireUuid);
        verify(mapper).modeleVersDto(commentaireModele);
    }

    @Test
    void trouverParUuid_DevraitRetournerEmpty_QuandNonExiste() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(commentaireService.lireParUuid(uuidInexistant))
                .thenReturn(Optional.empty());

        // When
        Optional<CommentaireDTO> resultat = apiAdapter.trouverParUuid(uuidInexistant);

        // Then
        assertThat(resultat).isEmpty();

        verify(commentaireService).lireParUuid(uuidInexistant);
        verify(mapper, never()).modeleVersDto(any());
    }

    @Test
    void trouverParDeal_DevraitRetournerListeCommentaires() {
        // Given
        CommentaireModele commentaire2 = CommentaireModele.builder()
                .uuid(UUID.randomUUID())
                .contenu("Bon deal")
                .note(4)
                .build();

        CommentaireDTO commentaireDTO2 = CommentaireDTO.builder()
                .uuid(commentaire2.getUuid())
                .contenu("Bon deal")
                .note(4)
                .build();

        List<CommentaireModele> commentairesModele = List.of(commentaireModele, commentaire2);

        when(commentaireService.lireTous(dealUuid)).thenReturn(commentairesModele);
        when(mapper.modeleVersDto(commentaireModele)).thenReturn(commentaireDTO);
        when(mapper.modeleVersDto(commentaire2)).thenReturn(commentaireDTO2);

        // When
        List<CommentaireDTO> resultat = apiAdapter.trouverParDeal(dealUuid);

        // Then
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getContenu()).isEqualTo("Excellent deal");
        assertThat(resultat.get(1).getContenu()).isEqualTo("Bon deal");

        verify(commentaireService).lireTous(dealUuid);
        verify(mapper, times(2)).modeleVersDto(any(CommentaireModele.class));
    }

    @Test
    void trouverParDeal_DevraitRetournerListeVide_QuandAucunCommentaire() {
        // Given
        when(commentaireService.lireTous(dealUuid)).thenReturn(List.of());

        // When
        List<CommentaireDTO> resultat = apiAdapter.trouverParDeal(dealUuid);

        // Then
        assertThat(resultat).isEmpty();

        verify(commentaireService).lireTous(dealUuid);
        verify(mapper, never()).modeleVersDto(any());
    }

    @Test
    void mettreAJour_DevraitMettreAJourCommentaire() {
        // Given
        CommentaireDTO dtoMisAJour = CommentaireDTO.builder()
                .uuid(commentaireUuid)
                .contenu("Commentaire mis à jour")
                .note(4)
                .utilisateurUuid(utilisateurUuid)
                .dealUuid(dealUuid)
                .build();

        CommentaireModele modeleMisAJour = CommentaireModele.builder()
                .uuid(commentaireUuid)
                .contenu("Commentaire mis à jour")
                .note(4)
                .build();

        when(mapper.dtoVersModele(dtoMisAJour)).thenReturn(modeleMisAJour);
        when(commentaireService.mettreAJour(commentaireUuid, modeleMisAJour))
                .thenReturn(modeleMisAJour);
        when(mapper.modeleVersDto(modeleMisAJour)).thenReturn(dtoMisAJour);

        // When
        CommentaireDTO resultat = apiAdapter.mettreAJour(commentaireUuid, dtoMisAJour);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(commentaireUuid);
        assertThat(resultat.getContenu()).isEqualTo("Commentaire mis à jour");
        assertThat(resultat.getNote()).isEqualTo(4);

        verify(mapper).dtoVersModele(dtoMisAJour);
        verify(commentaireService).mettreAJour(commentaireUuid, modeleMisAJour);
        verify(mapper).modeleVersDto(modeleMisAJour);
    }

    @Test
    void mettreAJour_DevraitDefinirUuidDansDTO() {
        // Given
        CommentaireDTO dtoSansUuid = CommentaireDTO.builder()
                .contenu("Commentaire")
                .note(5)
                .build();

        when(mapper.dtoVersModele(any(CommentaireDTO.class))).thenReturn(commentaireModele);
        when(commentaireService.mettreAJour(eq(commentaireUuid), any(CommentaireModele.class)))
                .thenReturn(commentaireModele);
        when(mapper.modeleVersDto(commentaireModele)).thenReturn(commentaireDTO);

        // When
        apiAdapter.mettreAJour(commentaireUuid, dtoSansUuid);

        // Then
        assertThat(dtoSansUuid.getUuid()).isEqualTo(commentaireUuid);

        verify(mapper).dtoVersModele(dtoSansUuid);
        verify(commentaireService).mettreAJour(commentaireUuid, commentaireModele);
    }

    @Test
    void supprimer_DevraitSupprimerCommentaire() {
        // Given
        doNothing().when(commentaireService).supprimerParUuid(commentaireUuid);

        // When
        apiAdapter.supprimer(commentaireUuid);

        // Then
        verify(commentaireService).supprimerParUuid(commentaireUuid);
    }
}

