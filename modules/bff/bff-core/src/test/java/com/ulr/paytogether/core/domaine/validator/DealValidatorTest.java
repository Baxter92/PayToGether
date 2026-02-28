package com.ulr.paytogether.core.domaine.validator;

import com.ulr.paytogether.core.enumeration.StatutDeal;
import com.ulr.paytogether.core.enumeration.StatutImage;
import com.ulr.paytogether.core.exception.ValidationException;
import com.ulr.paytogether.core.modele.CategorieModele;
import com.ulr.paytogether.core.modele.DealModele;
import com.ulr.paytogether.core.modele.ImageDealModele;
import com.ulr.paytogether.core.modele.UtilisateurModele;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DealValidator
 */
@ExtendWith(MockitoExtension.class)
class DealValidatorTest {

    @InjectMocks
    private DealValidator dealValidator;

    private DealModele dealModeleValide;
    private UUID uuidDeal;
    private UUID uuidCreateur;
    private UUID uuidCategorie;

    @BeforeEach
    void setUp() {
        uuidDeal = UUID.randomUUID();
        uuidCreateur = UUID.randomUUID();
        uuidCategorie = UUID.randomUUID();

        ImageDealModele image = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image.jpg")
                .isPrincipal(true)
                .statut(StatutImage.PENDING)
                .build();

        dealModeleValide = DealModele.builder()
                .uuid(uuidDeal)
                .titre("Deal valide")
                .description("Description du deal")
                .prixDeal(new BigDecimal("100.00"))
                .prixPart(new BigDecimal("25.00"))
                .nbParticipants(4)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(7))
                .statut(StatutDeal.PUBLIE)
                .createur(UtilisateurModele.builder().uuid(uuidCreateur).build())
                .categorie(CategorieModele.builder().uuid(uuidCategorie).build())
                .listeImages(List.of(image))
                .ville("Montreal")
                .pays("Canada")
                .build();
    }

    @Test
    void testValider_DevraitReussirAvecDealValide() {
        // When & Then
        assertDoesNotThrow(() -> dealValidator.valider(dealModeleValide));
    }

    @Test
    void testValider_DevraitEchouerSiDealNull() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(null)
        );
        assertEquals("deal.null", exception.getErrorCode());
    }

    @Test
    void testValider_DevraitEchouerSiTitreNull() {
        // Given
        dealModeleValide.setTitre(null);

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(dealModeleValide)
        );
        assertEquals("deal.titre.obligatoire", exception.getErrorCode());
    }

    @Test
    void testValider_DevraitEchouerSiTitreVide() {
        // Given
        dealModeleValide.setTitre("   ");

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(dealModeleValide)
        );
        assertEquals("deal.titre.obligatoire", exception.getErrorCode());
    }

    @Test
    void testValider_DevraitEchouerSiPrixDealNull() {
        // Given
        dealModeleValide.setPrixDeal(null);

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(dealModeleValide)
        );
        assertEquals("deal.prixDeal.obligatoire", exception.getErrorCode());
    }

    @Test
    void testValider_DevraitEchouerSiPrixDealNegatif() {
        // Given
        dealModeleValide.setPrixDeal(new BigDecimal("-10.00"));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(dealModeleValide)
        );
        assertEquals("deal.prixDeal.positif", exception.getErrorCode());
    }

    @Test
    void testValider_DevraitEchouerSiDescriptionTropLongue() {
        // Given
        dealModeleValide.setDescription("A".repeat(5001));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(dealModeleValide)
        );
        assertEquals("deal.description.longueur", exception.getErrorCode());
        assertArrayEquals(new Object[]{5000}, exception.getParams());
    }

    @Test
    void testValider_DevraitEchouerSiDateFinAvantDateDebut() {
        // Given
        dealModeleValide.setDateDebut(LocalDateTime.now().plusDays(10));
        dealModeleValide.setDateFin(LocalDateTime.now().plusDays(1));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(dealModeleValide)
        );
        assertEquals("deal.dateFin.coherence", exception.getErrorCode());
    }

    @Test
    void testValider_DevraitEchouerSiListeImagesVide() {
        // Given
        dealModeleValide.setListeImages(List.of());

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.valider(dealModeleValide)
        );
        assertEquals("deal.listeImages.obligatoire", exception.getErrorCode());
    }

    @Test
    void testValiderPourMiseAJourPartielle_DevraitReussirSanImageEtStatut() {
        // Given
        dealModeleValide.setListeImages(null);
        dealModeleValide.setStatut(null);

        // When & Then
        assertDoesNotThrow(() -> dealValidator.validerPourMiseAJourPartielle(dealModeleValide));
    }

    @Test
    void testValiderPourMiseAJourPartielle_DevraitEchouerSiUuidNull() {
        // Given
        dealModeleValide.setUuid(null);

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerPourMiseAJourPartielle(dealModeleValide)
        );
        assertEquals("deal.uuid.obligatoire", exception.getErrorCode());
    }

    @Test
    void testValiderTransitionStatut_DevraitReussirBrouillonVersPublie() {
        // When & Then
        assertDoesNotThrow(() ->
                dealValidator.validerTransitionStatut(StatutDeal.BROUILLON, StatutDeal.PUBLIE)
        );
    }

    @Test
    void testValiderTransitionStatut_DevraitReussirPublieVersExpire() {
        // When & Then
        assertDoesNotThrow(() ->
                dealValidator.validerTransitionStatut(StatutDeal.PUBLIE, StatutDeal.EXPIRE)
        );
    }

    @Test
    void testValiderTransitionStatut_DevraitEchouerBrouillonVersExpire() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerTransitionStatut(StatutDeal.BROUILLON, StatutDeal.EXPIRE)
        );
        assertEquals("deal.statut.transition.invalide", exception.getErrorCode());
    }

    @Test
    void testValiderTransitionStatut_DevraitEchouerSiExpireVersAutreStatut() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerTransitionStatut(StatutDeal.EXPIRE, StatutDeal.PUBLIE)
        );
        assertEquals("deal.statut.expire.immuable", exception.getErrorCode());
    }

    @Test
    void testValiderTransitionStatut_DevraitEchouerSiNouveauStatutNull() {
        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerTransitionStatut(StatutDeal.BROUILLON, null)
        );
        assertEquals("deal.statut.obligatoire", exception.getErrorCode());
    }

    @Test
    void testValiderImages_DevraitReussirAvecUneImagePrincipale() {
        // When & Then
        assertDoesNotThrow(() -> dealValidator.validerImages(dealModeleValide));
    }

    @Test
    void testValiderImages_DevraitEchouerSiListeImagesVide() {
        // Given
        dealModeleValide.setListeImages(List.of());

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerImages(dealModeleValide)
        );
        assertEquals("deal.listeImages.obligatoire", exception.getErrorCode());
    }

    @Test
    void testValiderImages_DevraitEchouerSiAucuneImagePrincipale() {
        // Given
        ImageDealModele image = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image.jpg")
                .isPrincipal(false)
                .build();
        dealModeleValide.setListeImages(List.of(image));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerImages(dealModeleValide)
        );
        assertEquals("deal.image.principale.manquante", exception.getErrorCode());
    }

    @Test
    void testValiderImages_DevraitEchouerSiPlusieursImagesPrincipales() {
        // Given
        ImageDealModele image1 = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image1.jpg")
                .isPrincipal(true)
                .build();
        ImageDealModele image2 = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage("image2.jpg")
                .isPrincipal(true)
                .build();
        dealModeleValide.setListeImages(List.of(image1, image2));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerImages(dealModeleValide)
        );
        assertEquals("deal.image.principale.unique", exception.getErrorCode());
    }

    @Test
    void testValiderImages_DevraitEchouerSiImageSansUrl() {
        // Given
        ImageDealModele image = ImageDealModele.builder()
                .uuid(UUID.randomUUID())
                .urlImage(null)
                .isPrincipal(true)
                .build();
        dealModeleValide.setListeImages(List.of(image));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dealValidator.validerImages(dealModeleValide)
        );
        assertEquals("deal.image.url.obligatoire", exception.getErrorCode());
    }
}

