package com.example.uniqueAproovaResidency.module.salary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSalaryRecordRequest {
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Month Year is required")
    private String monthYear;

    private BigDecimal advance = BigDecimal.ZERO;
    private BigDecimal deduction = BigDecimal.ZERO;
    private BigDecimal bonus = BigDecimal.ZERO;
    private String proofDocumentId;
}
