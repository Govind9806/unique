package com.example.uniqueAproovaResidency.module.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceAdjustmentRequest {

    @NotNull(message = "New balance is required")
    private BigDecimal newBalance;

    @NotBlank(message = "Reason for balance adjustment is mandatory and cannot be blank")
    private String reason;
}
