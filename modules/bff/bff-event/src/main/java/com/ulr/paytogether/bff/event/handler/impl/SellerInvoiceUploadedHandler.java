package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.event.SellerInvoiceUploadedEvent;
import com.ulr.paytogether.core.domaine.service.CommandeService;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.domaine.service.FileStorageService;
import com.ulr.paytogether.core.domaine.service.InvoiceGeneratorService;
import com.ulr.paytogether.core.domaine.service.PaiementService;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.PaiementModele;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler pour traiter l'événement d'upload de facture vendeur
 * Génère les factures PDF pour tous les clients et les envoie par email
 *
 * ⚠️ ARCHITECTURE HEXAGONALE :
 * - Ce handler fait partie de la PARTIE GAUCHE (bff-event - adaptateur d'entrée)
 * - Il appelle uniquement les SERVICES du CORE (bff-core)
 * - Il ne doit JAMAIS accéder directement aux Providers ou Repositories
 */
@Component
@RequiredArgsConstructor
public class SellerInvoiceUploadedHandler implements ConsumerHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SellerInvoiceUploadedHandler.class);

    private final CommandeService commandeService;
    private final PaiementService paiementService;
    private final InvoiceGeneratorService invoiceGeneratorService;
    private final EmailNotificationService emailNotificationService;
    private final FileStorageService fileStorageService;
    
    private static final String DIRECTORY_INVOICES_USER = "invoice/user/";

    /**
     * Traite l'événement d'upload de facture vendeur
     * Génère et envoie les factures à tous les clients de la commande
     *
     * @param event Événement d'upload de facture vendeur
     */
    @FunctionalHandler(
        eventType = SellerInvoiceUploadedEvent.class,
        maxAttempts = 3,
        description = "Génère et envoie les factures clients après upload de la facture vendeur"
    )
    public void handleSellerInvoiceUploaded(SellerInvoiceUploadedEvent event) {
        log.info("Traitement de l'événement SellerInvoiceUploadedEvent pour la commande: {}", event.getCommandeUuid());
        
        try {
            // Récupérer la commande (opération critique avec retry)
            CommandeModele commande = commandeService.lireParUuid(event.getCommandeUuid());
            if (commande == null) {
                throw new IllegalArgumentException("Commande non trouvée: " + event.getCommandeUuid());
            }
            
            // Récupérer tous les paiements de la commande (opération critique avec retry)
            List<PaiementModele> paiements = paiementService.lireParCommande(event.getCommandeUuid());
            if (paiements == null || paiements.isEmpty()) {
                log.warn("Aucun paiement trouvé pour la commande: {}", event.getCommandeUuid());
                return;
            }
            
            log.info("Génération de {} factures pour la commande {}", paiements.size(), event.getNumeroCommande());
            
            // Générer et envoyer les factures (isolé pour éviter retry des emails)
            genererEtEnvoyerFactures(commande, paiements, event);
            
            // Mettre à jour le statut de la commande à INVOICE_CUSTOMER
            commandeService.mettreAJourStatutCommande(event.getCommandeUuid(), 
                StatutCommande.INVOICE_CUSTOMER);
            
            log.info("✅ Statut de la commande {} mis à jour à INVOICE_CUSTOMER", event.getNumeroCommande());
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de SellerInvoiceUploadedEvent pour la commande {}: {}", 
                event.getCommandeUuid(), e.getMessage(), e);
            throw e; // Propagation pour retry automatique
        }
    }

    /**
     * Génère et envoie les factures à tous les clients.
     * Cette méthode ne propage PAS les exceptions pour éviter un retry complet qui renverrait tous les emails.
     */
    private void genererEtEnvoyerFactures(CommandeModele commande, List<PaiementModele> paiements, SellerInvoiceUploadedEvent event) {
        int facturesGenerees = 0;
        int facturesEnvoyees = 0;
        
        for (PaiementModele paiement : paiements) {
            try {
                // Déterminer si c'est une livraison à domicile ou pickup
                boolean isHomeDelivery = determinerTypeLivraison(paiement);
                String adresseLivraison = obtenirAdresseLivraison(paiement, isHomeDelivery);
                
                // Générer la facture PDF
                byte[] facturePdf = invoiceGeneratorService.genererFactureClient(
                    paiement,
                    commande.getNumeroCommande(),
                    commande.getDealModele().getTitre(),
                    isHomeDelivery,
                    adresseLivraison
                );
                
                facturesGenerees++;
                
                // Uploader la facture dans MinIO
                String nomFichierFacture = "invoice_" + commande.getNumeroCommande() + "_" 
                    + paiement.getUtilisateur().getUuid() + "_" 
                    + System.currentTimeMillis() + ".pdf";
                
                fileStorageService.uploadFile(
                    new ByteArrayInputStream(facturePdf),
                    nomFichierFacture,
                    DIRECTORY_INVOICES_USER,
                    facturePdf.length
                );
                
                log.info("Facture uploadée dans MinIO: {}", nomFichierFacture);
                
                // Générer l'URL présignée pour lecture
                String invoiceUrl = fileStorageService.generateReadUrl(
                    DIRECTORY_INVOICES_USER + nomFichierFacture
                );
                
                // Préparer les paramètres du template email
                Map<String, Object> templateParams = new HashMap<>();
                templateParams.put("clientNom", paiement.getUtilisateur().getPrenom() + " " + paiement.getUtilisateur().getNom());
                templateParams.put("numeroCommande", commande.getNumeroCommande());
                templateParams.put("dealTitre", commande.getDealModele().getTitre());
                templateParams.put("montantTotal", paiement.getMontant());
                templateParams.put("invoiceUrl", invoiceUrl);
                
                // Envoyer l'email avec la facture en pièce jointe
                emailNotificationService.envoyerEmailAvecPieceJointe(
                    paiement.getUtilisateur().getEmail(),
                    "Your Invoice - Order " + commande.getNumeroCommande(),
                    "invoice-client-email", // Template Thymeleaf
                    templateParams,
                    facturePdf,
                    nomFichierFacture
                );
                
                facturesEnvoyees++;
                log.info("✅ Facture envoyée au client {} pour le paiement {}", 
                    paiement.getUtilisateur().getEmail(), paiement.getUuid());
                
            } catch (Exception e) {
                // En cas d'erreur sur une facture, on log et on continue avec les autres
                // On ne propage PAS l'exception pour éviter un retry complet
                log.error("⚠️ Erreur lors de la génération/envoi de la facture pour le paiement {}: {}", 
                    paiement.getUuid(), e.getMessage(), e);
            }
        }
        
        log.info("Factures générées: {}, Factures envoyées: {} sur {} paiements pour la commande {}", 
            facturesGenerees, facturesEnvoyees, paiements.size(), event.getNumeroCommande());
        
        if (facturesEnvoyees == 0) {
            log.error("⚠️ Aucune facture n'a pu être envoyée pour la commande {}", event.getNumeroCommande());
            // On ne lance PAS d'exception pour éviter un retry qui renverrait tous les emails
        }
    }
    
    /**
     * Détermine si c'est une livraison à domicile ou pickup
     * TODO: Implémenter la logique réelle basée sur les données du paiement/commande
     */
    private boolean determinerTypeLivraison(PaiementModele paiement) {
        // Pour l'instant, on retourne false (pickup) par défaut
        // À adapter selon les données réelles disponibles
        return false;
    }
    
    /**
     * Obtient l'adresse de livraison ou de pickup
     * TODO: Implémenter la logique réelle basée sur les données du paiement/commande
     */
    private String obtenirAdresseLivraison(PaiementModele paiement, boolean isHomeDelivery) {
        // À adapter selon les données réelles disponibles
        if (isHomeDelivery) {
            // TODO: Récupérer l'adresse depuis les données de paiement/utilisateur
            return paiement.getUtilisateur().getEmail(); // Placeholder
        }
        return "Pickup at merchant location";
    }
}

