package com.example.uniqueAproovaResidency.module.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationResult {
    private boolean verified;
    private boolean successful;
    private String providerTransactionId;
    private String merchantReference;
    private BigDecimal amount;
    private String currency;
    private String failureReason;
}
