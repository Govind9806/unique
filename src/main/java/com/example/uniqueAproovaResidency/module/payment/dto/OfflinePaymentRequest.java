package com.example.uniqueAproovaResidency.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OfflinePaymentRequest {
    @NotBlank(message = "Bill ID is required")
    private String billId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // OFFLINE_UPI, CASH, BANK_TRANSFER

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;
}
