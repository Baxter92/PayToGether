package com.ulr.paytogether.bff.event.handler.impl;

import com.ulr.paytogether.bff.event.annotation.FunctionalHandler;
import com.ulr.paytogether.bff.event.handler.ConsumerHandler;
import com.ulr.paytogether.bff.event.model.DealCancelledEvent;
import com.ulr.paytogether.core.domaine.service.EmailNotificationService;
import com.ulr.paytogether.core.event.DealCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ulr.paytogether.bff.event.utils.EventUtils.DATE_FORMATTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class Dealhandler implements ConsumerHandler {

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;
    private final EmailNotificationService emailNotificationService;


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
        variables.put("lienDeal", construireLienverDeal(event.getDealUuid().toString()));

        // Appeler le service métier pour envoyer l'email
        emailNotificationService.envoyerNotification(
                event.getEmailMarchand(),
                "Nouveau deal créé : " + event.getTitreDeal(),
                "notification-deal-cree",
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
                "Deal annulé : " + event.getTitreDeal(),
                "notification-deal-annule",
                variables
        );

        log.info("Email sent successfully to: {}", event.getEmailMarchand());
    }

    /**
     * Construit le lien de validation avec le token
     */
    private String construireLienverDeal(String dealUuid) {
        return frontendBaseUrl + "/deals/%s".formatted(dealUuid);
    }

}
