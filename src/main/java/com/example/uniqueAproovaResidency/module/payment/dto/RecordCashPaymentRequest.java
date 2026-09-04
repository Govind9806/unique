package com.example.uniqueAproovaResidency.module.payment.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordCashPaymentRequest {
    private String flatNumber;
    private BigDecimal amount;
    private String remarks;
}
