package com.ulr.paytogether.provider.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.ulr.paytogether.core.domaine.service.InvoiceGeneratorService;
import com.ulr.paytogether.core.modele.PaiementModele;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

/**
 * Implémentation du service de génération de factures PDF
 * Utilise Thymeleaf pour les templates HTML et OpenHTMLToPDF pour la conversion en PDF
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceGeneratorServiceImpl implements InvoiceGeneratorService {
    
    private final TemplateEngine templateEngine;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final double TVA_RATE = 0.05; // 5% TVA
    private static final double SERVICE_FEE_RATE = 0.05; // 5% frais de service
    private static final double HOME_DELIVERY_FEE = 12.0; // Frais de livraison à domicile
    
    @Override
    public byte[] genererFactureClient(
            PaiementModele paiement,
            String numeroCommande,
            String dealTitre,
            boolean isHomeDelivery,
            String adresseLivraison
    ) throws IOException {
        log.info("Génération de la facture pour le paiement: {}", paiement.getUuid());
        
        try {
            // Calcul inverse pour retrouver le sous-total et les frais
            BigDecimal montantTotal = paiement.getMontant();
            
            // Calculer les composants de la facture
            InvoiceBreakdown breakdown = calculerComposantsFacture(montantTotal, isHomeDelivery);
            
            // Préparer le contexte Thymeleaf
            Context context = new Context();
            context.setVariable("numeroCommande", numeroCommande);
            context.setVariable("datePaiement", paiement.getDatePaiement().format(DATE_FORMATTER));
            context.setVariable("clientNom", paiement.getUtilisateur().getNom());
            context.setVariable("clientPrenom", paiement.getUtilisateur().getPrenom());
            context.setVariable("clientEmail", paiement.getUtilisateur().getEmail());
            context.setVariable("dealTitre", dealTitre);
            context.setVariable("adresseLivraison", adresseLivraison);
            context.setVariable("typelivraison", isHomeDelivery ? "Home Delivery" : "Pickup");
            
            // Montants
            context.setVariable("sousTotal", breakdown.sousTotal);
            context.setVariable("fraisService", breakdown.fraisService);
            context.setVariable("tva", breakdown.tva);
            context.setVariable("fraisLivraison", breakdown.fraisLivraison);
            context.setVariable("montantTotal", breakdown.montantTotal);
            context.setVariable("transactionId", paiement.getTransactionId());
            
            // Générer le HTML à partir du template
            String htmlContent = templateEngine.process("invoice-client", context);
            
            // Convertir en PDF
            byte[] pdfBytes = convertirHtmlEnPdf(htmlContent);
            
            log.info("Facture générée avec succès pour le paiement: {}", paiement.getUuid());
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération de la facture pour le paiement {}: {}", 
                paiement.getUuid(), e.getMessage(), e);
            throw new IOException("Échec de la génération de la facture", e);
        }
    }
    
    /**
     * Calcule les composants de la facture à partir du montant total
     */
    private InvoiceBreakdown calculerComposantsFacture(BigDecimal montantTotal, boolean isHomeDelivery) {
        InvoiceBreakdown breakdown = new InvoiceBreakdown();
        breakdown.montantTotal = montantTotal;
        
        // Frais de livraison
        breakdown.fraisLivraison = isHomeDelivery ? 
            BigDecimal.valueOf(HOME_DELIVERY_FEE) : BigDecimal.ZERO;
        
        // Calcul inverse:
        // Total = sousTotal + (sousTotal × 0.05) + ((sousTotal + sousTotal × 0.05) × 0.05) + fraisLivraison
        // Total = sousTotal × (1 + 0.05 + 0.05 + 0.05²) + fraisLivraison
        // Total = sousTotal × 1.1025 + fraisLivraison
        // sousTotal = (Total - fraisLivraison) / 1.1025

        BigDecimal montantSansFraisLivraison = montantTotal.subtract(breakdown.fraisLivraison);
        breakdown.sousTotal = montantSansFraisLivraison
            .divide(BigDecimal.valueOf(1.1025), 2, RoundingMode.HALF_UP);
        
        // Frais de service = sous-total × 5%
        breakdown.fraisService = breakdown.sousTotal
            .multiply(BigDecimal.valueOf(SERVICE_FEE_RATE))
            .setScale(2, RoundingMode.HALF_UP);

        // TVA = (sous-total + frais de service) × 5%
        BigDecimal baseAvantTva = breakdown.sousTotal.add(breakdown.fraisService);
        breakdown.tva = baseAvantTva
            .multiply(BigDecimal.valueOf(TVA_RATE))
            .setScale(2, RoundingMode.HALF_UP);
        
        return breakdown;
    }
    
    /**
     * Convertit le HTML en PDF avec OpenHTMLToPDF
     */
    private byte[] convertirHtmlEnPdf(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Classe interne pour stocker les composants de la facture
     */
    private static class InvoiceBreakdown {
        BigDecimal sousTotal;
        BigDecimal fraisService;
        BigDecimal tva;
        BigDecimal fraisLivraison;
        BigDecimal montantTotal;
    }
}

