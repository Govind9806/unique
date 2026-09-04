package com.example.uniqueAproovaResidency.module.work.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWorkRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String location;
    private String responsibleFlatId;
    private String vendorName;

    @NotNull(message = "Estimated cost is required")
    private BigDecimal estimatedCost;

    private java.util.List<String> photoUrls;
}
