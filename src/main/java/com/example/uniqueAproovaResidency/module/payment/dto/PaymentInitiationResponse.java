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
public class PaymentInitiationResponse {
    private String paymentId;
    private String billId;
    private String flatNumber;
    private BigDecimal amount;
    private String currency;
    private String merchantReference;
    private String payeeVpa;
    private String payeeName;
    private String upiUri;
    private String provider;
    private String status;
}
