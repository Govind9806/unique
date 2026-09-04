package com.example.uniqueAproovaResidency.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyPaymentRequest {
    @NotBlank(message = "Payment ID or Gateway Order ID is required")
    private String gatewayOrderId;

    @NotBlank(message = "Gateway Payment ID is required")
    private String gatewayPaymentId;

    private String gatewaySignature;
}
