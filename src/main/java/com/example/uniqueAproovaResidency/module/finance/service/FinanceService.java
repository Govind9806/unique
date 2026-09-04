package com.example.uniqueAproovaResidency.module.finance.service;

import com.example.uniqueAproovaResidency.module.apartment.entity.Apartment;
import com.example.uniqueAproovaResidency.module.apartment.repository.ApartmentRepository;
import com.example.uniqueAproovaResidency.module.expense.entity.Expense;
import com.example.uniqueAproovaResidency.module.expense.repository.ExpenseRepository;
import com.example.uniqueAproovaResidency.module.finance.dto.ExpenseBreakdownDto;
import com.example.uniqueAproovaResidency.module.finance.dto.FinanceSummaryDto;
import com.example.uniqueAproovaResidency.module.ledger.dto.LedgerTransactionDto;
import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final LedgerRepository ledgerRepository;
    private final ApartmentRepository apartmentRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public FinanceSummaryDto getSummary() {
        Apartment apt = apartmentRepository.findAll().stream().findFirst().orElse(null);
        BigDecimal opening = apt != null ? apt.getOpeningBalance() : BigDecimal.valueOf(50000);

        BigDecimal totalIncome = ledgerRepository.getTotalIncome();
        BigDecimal totalExpense = ledgerRepository.getTotalExpense();

        BigDecimal currentBalance = opening.add(totalIncome).subtract(totalExpense);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDay = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastDay = firstDay.plusMonths(1).minusNanos(1);

        BigDecimal mIncome = ledgerRepository.getMonthlyIncome(firstDay, lastDay);
        BigDecimal mExpense = ledgerRepository.getMonthlyExpense(firstDay, lastDay);
        BigDecimal netChange = mIncome.subtract(mExpense);

        ExpenseBreakdownDto breakdown = getExpenseBreakdown();

        List<LedgerTransactionDto> recent = ledgerRepository.findAllByOrderByTransactionDateDesc().stream()
                .limit(10)
                .map(LedgerTransactionDto::fromEntity)
                .collect(Collectors.toList());

        return FinanceSummaryDto.builder()
                .currentBalance(currentBalance)
                .openingBalance(opening)
                .monthlyIncome(mIncome)
                .monthlyExpense(mExpense)
                .netChange(netChange)
                .expenseBreakdown(breakdown)
                .recentTransactions(recent)
                .build();
    }

    @Transactional
    public FinanceSummaryDto updateOpeningBalance(BigDecimal newOpeningBalance) {
        Apartment apt = apartmentRepository.findAll().stream().findFirst().orElse(null);
        if (apt == null) {
            apt = Apartment.builder()
                    .id("apt-aproova")
                    .name("Aproova Residency")
                    .openingBalance(newOpeningBalance)
                    .build();
        } else {
            apt.setOpeningBalance(newOpeningBalance);
        }
        apartmentRepository.save(apt);
        return getSummary();
    }

    @Transactional(readOnly = true)
    public ExpenseBreakdownDto getExpenseBreakdown() {
        List<Expense> expenses = expenseRepository.findByApprovalStatus("APPROVED");

        BigDecimal water = BigDecimal.ZERO;
        BigDecimal electricity = BigDecimal.ZERO;
        BigDecimal repairs = BigDecimal.ZERO;
        BigDecimal salaries = BigDecimal.ZERO;
        BigDecimal tanker = BigDecimal.ZERO;
        BigDecimal other = BigDecimal.ZERO;

        for (Expense exp : expenses) {
            String cat = exp.getCategory().toUpperCase();
            BigDecimal amt = exp.getAmount();
            switch (cat) {
                case "WATER":
                    water = water.add(amt);
                    break;
                case "ELECTRICITY":
                    electricity = electricity.add(amt);
                    break;
                case "REPAIR":
                case "PIPELINE":
                case "PLUMBING":
                    repairs = repairs.add(amt);
                    break;
                case "SALARY":
                case "WATCHMAN_SALARY":
                case "CLEANER_SALARY":
                    salaries = salaries.add(amt);
                    break;
                case "TANKER":
                case "WATER_TANKER":
                    tanker = tanker.add(amt);
                    break;
                default:
                    other = other.add(amt);
                    break;
            }
        }

        return ExpenseBreakdownDto.builder()
                .water(water)
                .electricity(electricity)
                .repairs(repairs)
                .salaries(salaries)
                .tanker(tanker)
                .other(other)
                .build();
    }
}
