package com.example.uniqueAproovaResidency.module.water.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddWaterReadingRequest {
    @NotBlank(message = "Flat ID is required")
    private String flatId;

    @NotNull(message = "Current reading is required")
    private BigDecimal currentReading;

    private LocalDate readingDate;
    private String meterPhotoDocumentId;
}
