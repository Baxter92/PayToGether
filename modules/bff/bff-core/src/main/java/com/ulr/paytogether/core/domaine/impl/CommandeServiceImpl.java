package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.CommandeService;
import com.ulr.paytogether.core.enumeration.StatutCommande;
import com.ulr.paytogether.core.enumeration.StatutCommandeUtilisateur;
import com.ulr.paytogether.core.event.EventPublisher;
import com.ulr.paytogether.core.event.PayoutValidatedEvent;
import com.ulr.paytogether.core.event.SellerInvoiceUploadedEvent;
import com.ulr.paytogether.core.modele.CommandeModele;
import com.ulr.paytogether.core.modele.CommandeUtilisateurModele;
import com.ulr.paytogether.core.provider.CommandeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation du service Commande
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommandeServiceImpl implements CommandeService {

    private final CommandeProvider commandeProvider;
    private final EventPublisher eventPublisher;

    @Override
    public List<CommandeModele> lireToutesAvecInfosCompletes() {
        log.info("Récupération de toutes les commandes avec informations complètes");
        return commandeProvider.trouverToutesAvecInfosCompletes();
    }

    @Override
    public Map<String, Long> calculerStatistiques() {
        log.info("Calcul des statistiques des commandes");
        return commandeProvider.calculerStatistiquesCommandes();
    }

    @Override
    public CommandeModele lireParPaiementUuid(UUID paiementUuid) {
        log.info("Récupération de la commande par paiement UUID: {}", paiementUuid);
        if (paiementUuid == null) {
            log.warn("UUID de paiement est null");
            return null;
        }
        return commandeProvider.trouverParPaiementUuid(paiementUuid);
    }

    @Override
    public CommandeModele lireParUuid(UUID uuid) {
        return commandeProvider.lireParUuid(uuid);
    }
    
    @Override
    public CommandeModele validerPayout(UUID commandeUuid, LocalDateTime dateDepotPayout) {
        log.info("Validation du payout pour la commande: {}", commandeUuid);
        
        // Vérifier que la commande existe et est au statut COMPLETEE
        CommandeModele commande = commandeProvider.lireParUuid(commandeUuid);
        if (commande == null) {
            throw new IllegalArgumentException("Commande non trouvée avec l'UUID: " + commandeUuid);
        }
        
        if (commande.getStatut() != StatutCommande.COMPLETEE) {
            throw new IllegalStateException("La commande doit être au statut COMPLETEE pour valider le payout. Statut actuel: " + commande.getStatut());
        }
        
        // Mettre à jour le statut et la date de payout
        CommandeModele commandeMiseAJour = commandeProvider.mettreAJourStatutEtDatePayout(
            commandeUuid, 
            StatutCommande.PAYOUT, 
            dateDepotPayout
        );
        
        // Publier l'événement pour envoyer le mail au vendeur
        PayoutValidatedEvent event = PayoutValidatedEvent.builder()
            .commandeUuid(commandeUuid)
            .numeroCommande(commandeMiseAJour.getNumeroCommande())
            .vendeurUuid(commandeMiseAJour.getUtilisateur().getUuid())
            .emailVendeur(commandeMiseAJour.getUtilisateur().getEmail())
            .nomVendeur(commandeMiseAJour.getUtilisateur().getPrenom() + " " + commandeMiseAJour.getUtilisateur().getNom())
            .dateDepotPayout(dateDepotPayout)
            .build();
        
        eventPublisher.publishAsync(event);
        
        log.info("Payout validé pour la commande {} et événement publié", commandeUuid);
        return commandeMiseAJour;
    }
    
    @Override
    public CommandeModele uploadFactureVendeur(UUID commandeUuid, byte[] factureData, String factureNom) {
        log.info("Upload de la facture du vendeur pour la commande: {}", commandeUuid);
        
        // Vérifier que la commande existe et est au statut PAYOUT
        CommandeModele commande = commandeProvider.lireParUuid(commandeUuid);
        if (commande == null) {
            throw new IllegalArgumentException("Commande non trouvée avec l'UUID: " + commandeUuid);
        }
        
        if (commande.getStatut() != StatutCommande.PAYOUT) {
            throw new IllegalStateException("La commande doit être au statut PAYOUT pour uploader une facture. Statut actuel: " + commande.getStatut());
        }
        
        // Valider le fichier
        if (factureData == null || factureData.length == 0) {
            throw new IllegalArgumentException("Le fichier de facture est vide ou null");
        }
        
        if (factureNom == null || factureNom.isBlank()) {
            throw new IllegalArgumentException("Le nom du fichier de facture est obligatoire");
        }
        
        // Le fichier sera uploadé dans MinIO par le ProviderAdapter
        // L'URL sera retournée et stockée
        String factureUrl = "invoice/seller/" + commandeUuid + "_" + System.currentTimeMillis() + "_" + factureNom;
        
        // Mettre à jour la facture et le statut
        CommandeModele commandeMiseAJour = commandeProvider.mettreAJourFactureMarchand(commandeUuid, factureUrl);
        
        // Publier l'événement pour générer les factures clients
        SellerInvoiceUploadedEvent event = SellerInvoiceUploadedEvent.builder()
            .commandeUuid(commandeUuid)
            .numeroCommande(commandeMiseAJour.getNumeroCommande())
            .factureMarchandUrl(factureUrl)
            .build();
        
        eventPublisher.publishAsync(event);
        
        log.info("Facture uploadée pour la commande {} et événement publié", commandeUuid);
        return commandeMiseAJour;
    }
    
    @Override
    public Map<String, Object> validerFacturesClients(UUID commandeUuid, List<UUID> utilisateurUuids) {
        log.info("Validation des factures clients pour la commande: {}", commandeUuid);
        
        // Vérifier que la commande existe et est au statut INVOICE_CUSTOMER
        CommandeModele commande = commandeProvider.lireParUuid(commandeUuid);
        if (commande == null) {
            throw new IllegalArgumentException("Commande non trouvée avec l'UUID: " + commandeUuid);
        }
        
        if (commande.getStatut() != StatutCommande.INVOICE_CUSTOMER) {
            throw new IllegalStateException("La commande doit être au statut INVOICE_CUSTOMER pour valider les factures. Statut actuel: " + commande.getStatut());
        }
        
        // Valider chaque utilisateur
        for (UUID utilisateurUuid : utilisateurUuids) {
            commandeProvider.validerUtilisateurCommande(commandeUuid, utilisateurUuid);
        }
        
        // Vérifier si tous les utilisateurs sont validés
        boolean tousValides = commandeProvider.tousUtilisateursValides(commandeUuid);
        
        // Si tous validés, passer la commande en TERMINEE
        if (tousValides) {
            commandeProvider.mettreAJourStatutEtDatePayout(commandeUuid, StatutCommande.TERMINEE, null);
            log.info("Toutes les validations sont complètes, commande {} passée en TERMINEE", commandeUuid);
        }
        
        // Récupérer les statistiques
        List<CommandeUtilisateurModele> utilisateurs = commandeProvider.trouverUtilisateursCommande(commandeUuid);
        long nombreValidations = utilisateurs.stream()
            .filter(u -> u.getStatutCommandeUtilisateur() == StatutCommandeUtilisateur.VALIDEE)
            .count();
        
        Map<String, Object> resultat = new HashMap<>();
        resultat.put("commandeUuid", commandeUuid);
        resultat.put("numeroCommande", commande.getNumeroCommande());
        resultat.put("nombreValidations", nombreValidations);
        resultat.put("nombreTotal", utilisateurs.size());
        resultat.put("toutesValidees", tousValides);
        resultat.put("message", tousValides ? 
            "Toutes les factures sont validées, commande terminée" : 
            "Factures validées, en attente des autres validations");
        
        return resultat;
    }
    
    @Override
    public CommandeModele mettreAJourStatutCommande(UUID commandeUuid, StatutCommande nouveauStatut) {
        log.info("Mise à jour du statut de la commande {} vers {}", commandeUuid, nouveauStatut);
        
        // Vérifier que la commande existe
        CommandeModele commande = commandeProvider.lireParUuid(commandeUuid);
        if (commande == null) {
            throw new IllegalArgumentException("Commande non trouvée avec l'UUID: " + commandeUuid);
        }
        
        // Mettre à jour le statut
        CommandeModele commandeMiseAJour = commandeProvider.mettreAJourStatutEtDatePayout(
            commandeUuid, 
            nouveauStatut, 
            null
        );
        
        log.info("Statut de la commande {} mis à jour de {} vers {}", 
            commandeUuid, commande.getStatut(), nouveauStatut);
        
        return commandeMiseAJour;
    }

    @Override
    public List<CommandeUtilisateurModele> listerUtilisateursParCommande(UUID commandeUuid) {
        // Vérifier que la commande existe
        CommandeModele commande = commandeProvider.lireParUuid(commandeUuid);
        if (commande == null) {
            throw new IllegalArgumentException("Commande non trouvée avec l'UUID: " + commandeUuid);
        }

        if (commande.getStatut() != StatutCommande.FACTURES_CLIENT_ENVOYEES) {
            throw new IllegalStateException("La commande doit être au statut FACTURES_CLIENT_ENVOYEES pour reccupérer : " + commande.getStatut());
        }
        return commandeProvider.trouverUtilisateursCommande(commandeUuid);
    }
}

