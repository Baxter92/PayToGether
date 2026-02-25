package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.validator.CommentaireValidator;
import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.CommentaireModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import com.ulr.paytogether.core.provider.CommentaireProvider;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CommentaireServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class CommentaireServiceImplTest {

    @Mock
    private CommentaireProvider commentaireProvider;

    @Mock
    private CommentaireValidator commentaireValidator;

    @InjectMocks
    private CommentaireServiceImpl service;

    private CommentaireModele commentaireModele;
    private UUID commentaireUuid;
    private UUID dealUuid;
    private UUID utilisateurUuid;

    @BeforeEach
    void setUp() {
        commentaireUuid = UUID.randomUUID();
        dealUuid = UUID.randomUUID();
        utilisateurUuid = UUID.randomUUID();

        commentaireModele = CommentaireModele.builder()
                .uuid(commentaireUuid)
                .contenu("Excellent deal ! Je recommande.")
                .note(5)
                .utilisateur(UtilisateurModele.builder().uuid(utilisateurUuid).build())
                .deal(DealModele.builder().uuid(dealUuid).build())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();
    }

    @Test
    void creer_DevraitCreerCommentaire_QuandDonneesValides() {
        // Given
        when(commentaireProvider.sauvegarder(any(CommentaireModele.class)))
                .thenReturn(commentaireModele);

        // When
        CommentaireModele resultat = service.creer(commentaireModele);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getUuid()).isEqualTo(commentaireUuid);
        assertThat(resultat.getContenu()).isEqualTo("Excellent deal ! Je recommande.");
        assertThat(resultat.getNote()).isEqualTo(5);

        verify(commentaireValidator).valider(commentaireModele);
        verify(commentaireProvider).sauvegarder(commentaireModele);
    }

    @Test
    void creer_DevraitLeverValidationException_QuandContenuVide() {
        // Given
        CommentaireModele commentaireInvalide = CommentaireModele.builder()
                .contenu("")
                .note(5)
                .build();

        doThrow(new ValidationException("commentaire.contenu.obligatoire"))
                .when(commentaireValidator).valider(commentaireInvalide);

        // When & Then
        assertThatThrownBy(() -> service.creer(commentaireInvalide))
                .isInstanceOf(ValidationException.class)
                .hasMessage("commentaire.contenu.obligatoire");

        verify(commentaireValidator).valider(commentaireInvalide);
        verify(commentaireProvider, never()).sauvegarder(any());
    }

    @Test
    void creer_DevraitLeverValidationException_QuandNoteInvalide() {
        // Given
        CommentaireModele commentaireInvalide = CommentaireModele.builder()
                .contenu("Commentaire")
                .note(6)
                .build();

        doThrow(new ValidationException("commentaire.note.invalide"))
                .when(commentaireValidator).valider(commentaireInvalide);

        // When & Then
        assertThatThrownBy(() -> service.creer(commentaireInvalide))
                .isInstanceOf(ValidationException.class)
                .hasMessage("commentaire.note.invalide");

        verify(commentaireValidator).valider(commentaireInvalide);
        verify(commentaireProvider, never()).sauvegarder(any());
    }

    @Test
    void lireParUuid_DevraitRetournerCommentaire_QuandExiste() {
        // Given
        when(commentaireProvider.trouverParUuid(commentaireUuid))
                .thenReturn(Optional.of(commentaireModele));

        // When
        Optional<CommentaireModele> resultat = service.lireParUuid(commentaireUuid);

        // Then
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getUuid()).isEqualTo(commentaireUuid);
        assertThat(resultat.get().getContenu()).isEqualTo("Excellent deal ! Je recommande.");

        verify(commentaireProvider).trouverParUuid(commentaireUuid);
    }

    @Test
    void lireParUuid_DevraitRetournerEmpty_QuandNonExiste() {
        // Given
        UUID uuidInexistant = UUID.randomUUID();
        when(commentaireProvider.trouverParUuid(uuidInexistant))
                .thenReturn(Optional.empty());

        // When
        Optional<CommentaireModele> resultat = service.lireParUuid(uuidInexistant);

        // Then
        assertThat(resultat).isEmpty();

        verify(commentaireProvider).trouverParUuid(uuidInexistant);
    }

    @Test
    void lireTous_DevraitRetournerListeCommentaires() {
        // Given
        CommentaireModele commentaire2 = CommentaireModele.builder()
                .uuid(UUID.randomUUID())
                .contenu("Bon deal")
                .note(4)
                .build();

        List<CommentaireModele> commentaires = List.of(commentaireModele, commentaire2);
        when(commentaireProvider.trouverParDeal(dealUuid)).thenReturn(commentaires);

        // When
        List<CommentaireModele> resultat = service.lireTous(dealUuid);

        // Then
        assertThat(resultat).hasSize(2);
        assertThat(resultat).containsExactly(commentaireModele, commentaire2);

        verify(commentaireProvider).trouverParDeal(dealUuid);
    }

    @Test
    void lireTous_DevraitRetournerListeVide_QuandAucunCommentaire() {
        // Given
        when(commentaireProvider.trouverParDeal(dealUuid))
                .thenReturn(List.of());

        // When
        List<CommentaireModele> resultat = service.lireTous(dealUuid);

        // Then
        assertThat(resultat).isEmpty();

        verify(commentaireProvider).trouverParDeal(dealUuid);
    }

    @Test
    void mettreAJour_DevraitMettreAJourCommentaire_QuandDonneesValides() {
        // Given
        CommentaireModele commentaireMisAJour = CommentaireModele.builder()
                .uuid(commentaireUuid)
                .contenu("Commentaire mis à jour")
                .note(4)
                .utilisateur(UtilisateurModele.builder().uuid(utilisateurUuid).build())
                .deal(DealModele.builder().uuid(dealUuid).build())
                .build();

        when(commentaireProvider.sauvegarder(any(CommentaireModele.class)))
                .thenReturn(commentaireMisAJour);

        // When
        CommentaireModele resultat = service.mettreAJour(commentaireUuid, commentaireMisAJour);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getContenu()).isEqualTo("Commentaire mis à jour");
        assertThat(resultat.getNote()).isEqualTo(4);

        verify(commentaireValidator).validerPourMiseAJour(commentaireMisAJour);
        verify(commentaireProvider).sauvegarder(commentaireMisAJour);
    }

    @Test
    void mettreAJour_DevraitLeverValidationException_QuandUuidManquant() {
        // Given
        CommentaireModele commentaireSansUuid = CommentaireModele.builder()
                .contenu("Commentaire")
                .note(5)
                .build();

        doThrow(new ValidationException("commentaire.uuid.obligatoire"))
                .when(commentaireValidator).validerPourMiseAJour(commentaireSansUuid);

        // When & Then
        assertThatThrownBy(() -> service.mettreAJour(commentaireUuid, commentaireSansUuid))
                .isInstanceOf(ValidationException.class)
                .hasMessage("commentaire.uuid.obligatoire");

        verify(commentaireValidator).validerPourMiseAJour(commentaireSansUuid);
        verify(commentaireProvider, never()).sauvegarder(any());
    }

    @Test
    void supprimerParUuid_DevraitSupprimerCommentaire() {
        // Given
        doNothing().when(commentaireProvider).supprimerParUuid(commentaireUuid);

        // When
        service.supprimerParUuid(commentaireUuid);

        // Then
        verify(commentaireProvider).supprimerParUuid(commentaireUuid);
    }

    @Test
    void creer_DevraitLeverValidationException_QuandCommentaireNull() {
        // Given
        doThrow(new ValidationException("commentaire.null"))
                .when(commentaireValidator).valider(null);

        // When & Then
        assertThatThrownBy(() -> service.creer(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("commentaire.null");

        verify(commentaireValidator).valider(null);
        verify(commentaireProvider, never()).sauvegarder(any());
    }

    @Test
    void creer_DevraitLeverValidationException_QuandUtilisateurManquant() {
        // Given
        CommentaireModele commentaireSansUtilisateur = CommentaireModele.builder()
                .contenu("Commentaire")
                .note(5)
                .deal(DealModele.builder().uuid(dealUuid).build())
                .build();

        doThrow(new ValidationException("commentaire.utilisateurUuid.obligatoire"))
                .when(commentaireValidator).valider(commentaireSansUtilisateur);

        // When & Then
        assertThatThrownBy(() -> service.creer(commentaireSansUtilisateur))
                .isInstanceOf(ValidationException.class)
                .hasMessage("commentaire.utilisateurUuid.obligatoire");

        verify(commentaireValidator).valider(commentaireSansUtilisateur);
        verify(commentaireProvider, never()).sauvegarder(any());
    }

    @Test
    void creer_DevraitLeverValidationException_QuandDealManquant() {
        // Given
        CommentaireModele commentaireSansDeal = CommentaireModele.builder()
                .contenu("Commentaire")
                .note(5)
                .utilisateur(UtilisateurModele.builder().uuid(utilisateurUuid).build())
                .build();

        doThrow(new ValidationException("commentaire.dealUuid.obligatoire"))
                .when(commentaireValidator).valider(commentaireSansDeal);

        // When & Then
        assertThatThrownBy(() -> service.creer(commentaireSansDeal))
                .isInstanceOf(ValidationException.class)
                .hasMessage("commentaire.dealUuid.obligatoire");

        verify(commentaireValidator).valider(commentaireSansDeal);
        verify(commentaireProvider, never()).sauvegarder(any());
    }
}

