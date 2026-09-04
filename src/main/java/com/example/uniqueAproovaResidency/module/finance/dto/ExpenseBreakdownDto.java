package com.example.uniqueAproovaResidency.module.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseBreakdownDto {
    private BigDecimal water;
    private BigDecimal electricity;
    private BigDecimal repairs;
    private BigDecimal salaries;
    private BigDecimal tanker;
    private BigDecimal other;
}
