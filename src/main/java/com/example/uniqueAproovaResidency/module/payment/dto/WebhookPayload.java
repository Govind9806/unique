package com.example.uniqueAproovaResidency.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {

    @NotBlank(message = "Merchant reference / Payment ID is required")
    private String merchantReference;

    @NotBlank(message = "Provider transaction ID / Bank UTR is required")
    private String providerTransactionId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Builder.Default
    private String currency = "INR";

    @Builder.Default
    private String status = "SUCCESS"; // SUCCESS, FAILED, CANCELLED

    private String signature;
    private String payeeVpa;
    private String failureReason;
}
