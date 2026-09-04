package com.example.uniqueAproovaResidency.module.finance.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.finance.dto.ExpenseBreakdownDto;
import com.example.uniqueAproovaResidency.module.finance.dto.FinanceSummaryDto;
import com.example.uniqueAproovaResidency.module.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@Tag(name = "Apartment Finance", description = "Endpoints supporting the Figma Apartment Money screen with live ledger balance and breakdown")
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/summary")
    @Operation(summary = "Get Apartment Financial Summary (Current Balance, Collection, Expense Breakdown)")
    public ResponseEntity<ApiResponse<FinanceSummaryDto>> getSummary() {
        FinanceSummaryDto summary = financeService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/opening-balance")
    @Operation(summary = "Update Society Starting Opening Balance")
    public ResponseEntity<ApiResponse<FinanceSummaryDto>> updateOpeningBalance(@RequestParam BigDecimal newOpeningBalance) {
        FinanceSummaryDto summary = financeService.updateOpeningBalance(newOpeningBalance);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/expense-breakdown")
    @Operation(summary = "Get Categorized Expense Breakdown")
    public ResponseEntity<ApiResponse<ExpenseBreakdownDto>> getExpenseBreakdown() {
        ExpenseBreakdownDto breakdown = financeService.getExpenseBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }
}
