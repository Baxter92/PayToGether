package com.ulr.paytogether.core.domaine.impl;

import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.provider.EmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implémentation du service d'envoi de notifications par email
 * Orchestration métier - Pas de règles métier complexes ici (juste orchestration)
 * Délègue l'envoi au Provider (infrastructure)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final EmailProvider emailProvider;

    @Override
    public void envoyerNotification(String destinataire, String sujet, String templateName, Map<String, Object> variables) {
        log.info("Service - Envoi de notification email à: {}", destinataire);

        // Pas de règle métier complexe ici
        // Juste orchestration et délégation au provider
        var checkFakeEmail = destinataire.contains("example")
                || destinataire.contains("test");
        String destinataireFinal = checkFakeEmail ? "ulrsinha@gmail.com" : destinataire;
        emailProvider.envoyerEmail(destinataireFinal, sujet, templateName, variables);

        log.debug("Service - Notification déléguée au provider pour: {}", destinataire);
    }
    
    @Override
    public void envoyerEmailAvecTemplate(String destinataire, String sujet, String templateName, Map<String, Object> templateParams) {
        log.info("Service - Envoi d'email avec template {} à: {}", templateName, destinataire);
        
        var checkFakeEmail = destinataire.contains("example") || destinataire.contains("test");
        String destinataireFinal = checkFakeEmail ? "ulrsinha@gmail.com" : destinataire;
        
        emailProvider.envoyerEmail(destinataireFinal, sujet, "notifications/" + templateName, templateParams);
        
        log.debug("Service - Email avec template délégué au provider pour: {}", destinataire);
    }
    
    @Override
    public void envoyerEmailAvecPieceJointe(String destinataire, String sujet, String templateName, 
                                             Map<String, Object> templateParams, byte[] attachmentData, String attachmentName) {
        log.info("Service - Envoi d'email avec pièce jointe {} à: {}", attachmentName, destinataire);
        
        var checkFakeEmail = destinataire.contains("example") || destinataire.contains("test");
        String destinataireFinal = checkFakeEmail ? "ulrsinha@gmail.com" : destinataire;
        
        emailProvider.envoyerEmailAvecPieceJointe(destinataireFinal, sujet, "notifications/" + templateName, 
                                                  templateParams, attachmentData, attachmentName);
        
        log.debug("Service - Email avec pièce jointe délégué au provider pour: {}", destinataire);
    }
}

