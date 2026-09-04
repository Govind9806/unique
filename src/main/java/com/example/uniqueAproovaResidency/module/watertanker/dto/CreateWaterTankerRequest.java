package com.example.uniqueAproovaResidency.module.watertanker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateWaterTankerRequest {
    @NotBlank(message = "Supplier name is required")
    private String supplier;

    private LocalDate tankerDate;

    @NotNull(message = "Quantity in liters is required")
    private Integer quantityLiters;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String reason;
    private String invoiceDocumentId;
}
