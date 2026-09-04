package com.example.uniqueAproovaResidency.module.water.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterSummaryDto {
    private int totalMeters;
    private BigDecimal totalConsumptionThisMonth;
    private BigDecimal totalWaterExpensesThisMonth;
    private int tankerCountThisMonth;
    private List<WaterReadingDto> recentReadings;
}
