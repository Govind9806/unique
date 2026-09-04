package com.example.uniqueAproovaResidency.module.responsibility.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateResponsibilityRequest {
    @NotBlank(message = "Responsibility type is required")
    private String responsibilityType;

    @NotBlank(message = "Flat ID is required")
    private String flatId;

    private String assignedUserId;
    private LocalDate startDate;
}
