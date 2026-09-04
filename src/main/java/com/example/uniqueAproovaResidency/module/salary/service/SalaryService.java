package com.example.uniqueAproovaResidency.module.salary.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.employee.entity.Employee;
import com.example.uniqueAproovaResidency.module.employee.repository.EmployeeRepository;
import com.example.uniqueAproovaResidency.module.expense.entity.Expense;
import com.example.uniqueAproovaResidency.module.expense.repository.ExpenseRepository;
import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
import com.example.uniqueAproovaResidency.module.salary.dto.CreateSalaryRecordRequest;
import com.example.uniqueAproovaResidency.module.salary.dto.SalaryRecordDto;
import com.example.uniqueAproovaResidency.module.salary.entity.SalaryRecord;
import com.example.uniqueAproovaResidency.module.salary.repository.SalaryRecordRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryService {

    private final SalaryRecordRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final ExpenseRepository expenseRepository;
    private final LedgerRepository ledgerRepository;

    @Transactional(readOnly = true)
    public List<SalaryRecordDto> getAllSalaries() {
        return salaryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(SalaryRecordDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public SalaryRecordDto calculateAndCreateSalary(CreateSalaryRecordRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        BigDecimal base = emp.getMonthlySalary();
        BigDecimal advance = request.getAdvance() != null ? request.getAdvance() : BigDecimal.ZERO;
        BigDecimal deduction = request.getDeduction() != null ? request.getDeduction() : BigDecimal.ZERO;
        BigDecimal bonus = request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO;

        BigDecimal finalAmount = base.add(bonus).subtract(advance).subtract(deduction);

        SalaryRecord record = SalaryRecord.builder()
                .id(UUID.randomUUID().toString())
                .employee(emp)
                .monthYear(request.getMonthYear())
                .baseSalary(base)
                .advance(advance)
                .deduction(deduction)
                .bonus(bonus)
                .finalAmount(finalAmount)
                .status("PENDING")
                .proofDocumentId(request.getProofDocumentId())
                .build();

        SalaryRecord saved = salaryRepository.save(record);
        return SalaryRecordDto.fromEntity(saved);
    }

    @Transactional
    public SalaryRecordDto paySalary(String id, User payer) {
        SalaryRecord record = salaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryRecord", "id", id));

        if ("PAID".equalsIgnoreCase(record.getStatus())) {
            return SalaryRecordDto.fromEntity(record);
        }

        record.setStatus("PAID");
        record.setPaymentDate(LocalDate.now());
        SalaryRecord saved = salaryRepository.save(record);

        // 1. Create Expense
        Expense expense = Expense.builder()
                .id(UUID.randomUUID().toString())
                .category("SALARY")
                .description(saved.getEmployee().getName() + " (" + saved.getEmployee().getRole() + ") Salary for " + saved.getMonthYear())
                .amount(saved.getFinalAmount())
                .expenseDate(LocalDate.now())
                .vendor(saved.getEmployee().getName())
                .createdBy(payer)
                .approvalStatus("APPROVED")
                .paymentStatus("PAID")
                .approvedBy(payer)
                .approvedAt(LocalDateTime.now())
                .build();
        expenseRepository.save(expense);

        // 2. Create Immutable Ledger EXPENSE transaction
        if (!ledgerRepository.existsByReferenceTypeAndReferenceId("SALARY", saved.getId())) {
            String cat = saved.getEmployee().getRole().equalsIgnoreCase("WATCHMAN") ? "WATCHMAN_SALARY" : "CLEANER_SALARY";
            LedgerTransaction ledgerTx = LedgerTransaction.builder()
                    .id(UUID.randomUUID().toString())
                    .type("EXPENSE")
                    .category(cat)
                    .amount(saved.getFinalAmount())
                    .description(saved.getEmployee().getName() + " Salary for " + saved.getMonthYear())
                    .referenceType("SALARY")
                    .referenceId(saved.getId())
                    .transactionDate(LocalDateTime.now())
                    .createdBy(payer)
                    .build();
            ledgerRepository.save(ledgerTx);
        }

        return SalaryRecordDto.fromEntity(saved);
    }
}
