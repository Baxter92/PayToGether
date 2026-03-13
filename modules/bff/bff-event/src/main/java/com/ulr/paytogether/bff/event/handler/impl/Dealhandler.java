package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.event.DealCancelledEvent;
import com.ulr.paytogether.core.event.DealCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ulr.paytogether.bff.event.utils.EventUtils.CONSTRUIRELIENDEAL;
import static com.ulr.paytogether.bff.event.utils.EventUtils.DATE_FORMATTER;

@Component
@RequiredArgsConstructor
public class Dealhandler implements ConsumerHandler {

    private static final Logger log = LoggerFactory.getLogger(Dealhandler.class);

    private final EmailNotificationService emailNotificationService;
    
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;


    @FunctionalHandler(
        eventType = DealCreatedEvent.class,
        description = "Handler pour traiter les événements de création de deal"
    )
    public void handleDealCreatedEvent(DealCreatedEvent event) {
        log.info("Received DealCreatedEvent: for dealUUid {}", event.getDealUuid());

        Map<String, Object> variables = new HashMap<>();
        variables.put("prenom", event.getPrenomMarchand());
        variables.put("nom", event.getNomMarchand());
        variables.put("titreDeal", event.getTitreDeal());
        variables.put("descriptionDeal", event.getDescriptionDeal());
        variables.put("prixDealPart", event.getMontantPart());
        variables.put("prixDeal", event.getMontant());
        variables.put("dateCreation", event.getDateCreation().format(DATE_FORMATTER));
        variables.put("nbParticipants", event.getNbParticipants());
        variables.put("lienDeal", CONSTRUIRELIENDEAL(event.getDealUuid().toString(), frontendBaseUrl));

        // Appeler le service métier pour envoyer l'email
        emailNotificationService.envoyerNotification(
                event.getEmailMarchand(),
                "New deal created : " + event.getTitreDeal(),
                "notification-deal-cree-en",
                variables
        );

        log.info("Email sent successfully to: {}", event.getEmailMarchand());
    }

    @FunctionalHandler(
            eventType = DealCreatedEvent.class,
            description = "Handler pour traiter les événements de création de deal"
    )
    public void handleDealDeletedEvent(DealCancelledEvent event) {
        log.info("Received DealCreatedEvent: for dealUUid {}", event.getDealUuid());

        Map<String, Object> variables = new HashMap<>();
        variables.put("prenom", event.getPrenomMarchand());
        variables.put("nom", event.getNomMarchand());
        variables.put("titreDeal", event.getTitreDeal());
        variables.put("dateAnnulation", event.getDateAnnulation());
        variables.put("raisonAnnulation", event.getRaisonAnnulation());
        // Appeler le service métier pour envoyer l'email
        emailNotificationService.envoyerNotification(
                event.getEmailMarchand(),
                "Deal canceled : " + event.getTitreDeal(),
                "notification-deal-annule-en",
                variables
        );

        log.info("Email sent successfully to: {}", event.getEmailMarchand());
    }



}
