package com.example.uniqueAproovaResidency.module.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateExpenseRequest {
    @NotBlank(message = "Category is required")
    private String category; // WATER, ELECTRICITY, REPAIR, SALARY, TANKER, MAINTENANCE, OTHER

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private LocalDate expenseDate;
    private String vendor;
    private String relatedWorkId;
    private String responsibleFlatId;
    private Boolean isEmergency = false;
    private String photoUrl;
    private String receiptPhotoDocumentId;
}
