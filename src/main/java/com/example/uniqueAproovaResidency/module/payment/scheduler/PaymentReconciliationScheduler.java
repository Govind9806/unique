package com.example.uniqueAproovaResidency.module.payment.scheduler;

import com.example.uniqueAproovaResidency.module.payment.dto.PaymentVerificationResult;
import com.example.uniqueAproovaResidency.module.payment.dto.WebhookPayload;
import com.example.uniqueAproovaResidency.module.payment.entity.Payment;
import com.example.uniqueAproovaResidency.module.payment.provider.PaymentProvider;
import com.example.uniqueAproovaResidency.module.payment.provider.PaymentProviderFactory;
import com.example.uniqueAproovaResidency.module.payment.repository.PaymentRepository;
import com.example.uniqueAproovaResidency.module.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentProviderFactory paymentProviderFactory;

    @Scheduled(cron = "0 */15 * * * ?") // Runs every 15 minutes
    public void reconcilePendingPayments() {
        log.info("Running automatic background payment reconciliation for pending UPI transactions...");

        List<Payment> pendingPayments = paymentRepository.findByStatus("INITIATED");
        pendingPayments.addAll(paymentRepository.findByStatus("PENDING"));

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        PaymentProvider provider = paymentProviderFactory.getProvider();

        for (Payment payment : pendingPayments) {
            if (payment.getCreatedAt() != null && payment.getCreatedAt().isBefore(cutoff)) {
                try {
                    PaymentVerificationResult result = provider.verifyPayment(
                            payment.getGatewayPaymentId(), payment.getGatewayOrderId()
                    );

                    if (result.isSuccessful()) {
                        log.info("Reconciliation confirmed successful payment for reference: {}", payment.getGatewayOrderId());
                        WebhookPayload payload = WebhookPayload.builder()
                                .merchantReference(payment.getGatewayOrderId())
                                .providerTransactionId(result.getProviderTransactionId() != null ? result.getProviderTransactionId() : "RECON-" + payment.getId())
                                .amount(payment.getAmount())
                                .status("SUCCESS")
                                .build();
                        paymentService.processWebhook(payload, null);
                    }
                } catch (Exception e) {
                    log.error("Failed to reconcile payment ID: {}", payment.getId(), e);
                }
            }
        }
    }
}
