package com.example.uniqueAproovaResidency.module.payment.provider;

import com.example.uniqueAproovaResidency.module.payment.dto.PaymentInitiationResponse;
import com.example.uniqueAproovaResidency.module.payment.dto.PaymentVerificationResult;
import com.example.uniqueAproovaResidency.module.payment.dto.WebhookPayload;
import com.example.uniqueAproovaResidency.module.payment.entity.Payment;

public interface PaymentProvider {
    String getProviderName();

    PaymentInitiationResponse initiatePayment(Payment payment, String payeeVpa, String payeeName, String note);

    PaymentVerificationResult verifyPayment(String providerTransactionId, String merchantReference);

    PaymentVerificationResult processWebhook(WebhookPayload payload, String rawSignature, String webhookSecret);

    boolean refundPayment(Payment payment, String reason);
}
