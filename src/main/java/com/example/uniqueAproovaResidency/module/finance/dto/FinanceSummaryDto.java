package com.example.uniqueAproovaResidency.module.finance.dto;

import com.example.uniqueAproovaResidency.module.ledger.dto.LedgerTransactionDto;
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
public class FinanceSummaryDto {
    private BigDecimal currentBalance;
    private BigDecimal openingBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal netChange;
    private ExpenseBreakdownDto expenseBreakdown;
    private List<LedgerTransactionDto> recentTransactions;
}
