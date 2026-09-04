package com.example.uniqueAproovaResidency.module.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private String billId;
    private String flatNumber;
    private String paymentMode = "CURRENT_MONTH"; // CURRENT_MONTH, PAST_ARREARS, ALL_DUES
    private BigDecimal amount;
    private String paymentMethod = "UPI_INTENT";
}
