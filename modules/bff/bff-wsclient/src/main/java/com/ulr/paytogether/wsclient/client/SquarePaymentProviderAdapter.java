package com.ulr.paytogether.wsclient.client;

import com.squareup.square.SquareClient;
import com.squareup.square.core.SquareApiException;
import com.squareup.square.types.*;
import com.ulr.paytogether.core.provider.SquarePaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Implémentation du provider Square Payment.
 * Gère les appels à l'API Square pour les paiements.
 * Compatible avec Square Java SDK version 46.0.0.20260122
 *
 * Documentation : https://github.com/square/square-java-sdk
 *
 * @author PayToGether Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SquarePaymentProviderAdapter implements SquarePaymentProvider {

    private final SquareClient squareClient;
    private final String squareLocationId;

    @Override
    public String creerPaiement(String squareToken, BigDecimal montant, String locationId, String referenceId) {
        log.info("Creating Square payment: amount={}, locationId={}, referenceId={}",
                 montant, locationId, referenceId);

        try {
            // Convertir le montant en centimes (Square utilise les plus petites unités)
            long amountInCents = montant.multiply(new BigDecimal("100")).longValue();

            Money money = Money.builder()
                .amount(amountInCents)
                .currency(Currency.CAD) // Devise canadienne
                .build();

            CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .sourceId(squareToken)
                .idempotencyKey(UUID.randomUUID().toString())
                .amountMoney(money)
                .locationId(locationId != null ? locationId : squareLocationId)
                .referenceId(referenceId)
                .note("Paiement via PayToGether")
                .autocomplete(true)
                .build();

            CreatePaymentResponse response = squareClient.payments().create(createPaymentRequest);

            String paymentId = response.getPayment()
                .flatMap(Payment::getId)
                .orElseThrow(() -> new RuntimeException("Square payment response is null"));

            log.info("Square payment created successfully: paymentId={}", paymentId);
            return paymentId;

        } catch (SquareApiException e) {
            log.error("Square API error creating payment: {}", e.getMessage());
            throw new RuntimeException("Erreur Square API lors de la création du paiement: " +
                e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating Square payment: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur inattendue lors de la création du paiement Square", e);
        }
    }

    @Override
    public String verifierStatutPaiement(String squarePaymentId) {
        log.info("Verifying Square payment status: paymentId={}", squarePaymentId);

        try {
            GetPaymentsRequest getRequest = GetPaymentsRequest.builder()
                .paymentId(squarePaymentId)
                .build();

            GetPaymentResponse response = squareClient.payments().get(getRequest);

            String status = response.getPayment()
                .flatMap(Payment::getStatus)
                .orElseThrow(() -> new RuntimeException("Square payment not found: " + squarePaymentId));

            log.info("Square payment status: paymentId={}, status={}", squarePaymentId, status);
            return status;

        } catch (SquareApiException e) {
            log.error("Square API error verifying payment: {}", e.getMessage());
            throw new RuntimeException("Erreur Square API lors de la vérification du paiement: " +
                e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error verifying Square payment: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur inattendue lors de la vérification du paiement Square", e);
        }
    }

    @Override
    public String recupererUrlRecu(String squarePaymentId) {
        log.info("Retrieving Square payment receipt URL: paymentId={}", squarePaymentId);

        try {
            GetPaymentsRequest getRequest = GetPaymentsRequest.builder()
                .paymentId(squarePaymentId)
                .build();

            GetPaymentResponse response = squareClient.payments().get(getRequest);

            String receiptUrl = response.getPayment()
                .flatMap(Payment::getReceiptUrl)
                .orElse(null);

            if (receiptUrl != null) {
                log.info("Square payment receipt URL retrieved: {}", receiptUrl);
            } else {
                log.warn("No receipt URL available for payment: {}", squarePaymentId);
            }

            return receiptUrl;

        } catch (SquareApiException e) {
            log.error("Square API error retrieving receipt URL: {}", e.getMessage());
            throw new RuntimeException("Erreur Square API lors de la récupération du reçu: " +
                e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving Square receipt URL: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur inattendue lors de la récupération du reçu Square", e);
        }
    }

    @Override
    public String rembourserPaiement(String squarePaymentId, BigDecimal montant, String raison) {
        log.info("Refunding Square payment: paymentId={}, amount={}, reason={}",
                 squarePaymentId, montant, raison);

        try {
            // Convertir le montant en centimes
            long amountInCents = montant.multiply(new BigDecimal("100")).longValue();

            Money money = Money.builder()
                .amount(amountInCents)
                .currency(Currency.CAD)
                .build();

            RefundPaymentRequest refundRequest = RefundPaymentRequest.builder()
                .idempotencyKey(UUID.randomUUID().toString())
                .amountMoney(money)
                .paymentId(squarePaymentId)
                .reason(raison)
                .build();

            RefundPaymentResponse response = squareClient.refunds().refundPayment(refundRequest);

            String refundId = response.getRefund()
                .map(PaymentRefund::getId)
                .orElseThrow(() -> new RuntimeException("Square refund response is null"));

            log.info("Square payment refunded successfully: refundId={}", refundId);
            return refundId;

        } catch (SquareApiException e) {
            log.error("Square API error refunding payment: {}", e.getMessage());
            throw new RuntimeException("Erreur Square API lors du remboursement: " +
                e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error refunding Square payment: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur inattendue lors du remboursement Square", e);
        }
    }

    @Override
    public SquarePaymentDetails recupererDetailsPaiement(String squarePaymentId) {
        log.info("Retrieving Square payment details: paymentId={}", squarePaymentId);

        try {
            GetPaymentsRequest getRequest = GetPaymentsRequest.builder()
                .paymentId(squarePaymentId)
                .build();

            GetPaymentResponse response = squareClient.payments().get(getRequest);

            Payment payment = response.getPayment()
                .orElseThrow(() -> new RuntimeException("Square payment not found: " + squarePaymentId));

            BigDecimal montant = payment.getAmountMoney()
                .flatMap(money -> money.getAmount()
                    .map(amt -> new BigDecimal(amt)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)))
                .orElse(BigDecimal.ZERO);

            SquarePaymentDetails details = new SquarePaymentDetails(
                payment.getId().orElse(null),
                payment.getOrderId().orElse(null),
                payment.getStatus().orElse(null),
                montant,
                payment.getReceiptUrl().orElse(null),
                payment.getLocationId().orElse(null)
            );

            log.info("Square payment details retrieved successfully: {}", details);
            return details;

        } catch (SquareApiException e) {
            log.error("Square API error retrieving payment details: {}", e.getMessage());
            throw new RuntimeException("Erreur Square API lors de la récupération des détails: " +
                e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving Square payment details: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur inattendue lors de la récupération des détails Square", e);
        }
    }
}

