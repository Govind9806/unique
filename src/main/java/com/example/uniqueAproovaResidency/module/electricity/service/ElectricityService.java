package com.example.uniqueAproovaResidency.module.electricity.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.electricity.dto.ElectricityBillDto;
import com.example.uniqueAproovaResidency.module.electricity.entity.ElectricityBill;
import com.example.uniqueAproovaResidency.module.electricity.repository.ElectricityBillRepository;
import com.example.uniqueAproovaResidency.module.expense.entity.Expense;
import com.example.uniqueAproovaResidency.module.expense.repository.ExpenseRepository;
import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
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
public class ElectricityService {

    private final ElectricityBillRepository billRepository;
    private final ExpenseRepository expenseRepository;
    private final LedgerRepository ledgerRepository;

    @Transactional(readOnly = true)
    public List<ElectricityBillDto> getAllElectricityBills() {
        return billRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ElectricityBillDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ElectricityBillDto getElectricityBillById(String id) {
        ElectricityBill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ElectricityBill", "id", id));
        return ElectricityBillDto.fromEntity(bill);
    }

    @Transactional
    public ElectricityBillDto createElectricityBill(String billingMonth, BigDecimal amount, LocalDate dueDate, String documentId, User creator) {
        ElectricityBill bill = ElectricityBill.builder()
                .id(UUID.randomUUID().toString())
                .billingMonth(billingMonth)
                .amount(amount)
                .dueDate(dueDate)
                .status("PENDING")
                .documentId(documentId)
                .createdBy(creator)
                .build();

        ElectricityBill saved = billRepository.save(bill);
        return ElectricityBillDto.fromEntity(saved);
    }

    @Transactional
    public ElectricityBillDto payElectricityBill(String id, User payer) {
        ElectricityBill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ElectricityBill", "id", id));

        if ("PAID".equalsIgnoreCase(bill.getStatus())) {
            return ElectricityBillDto.fromEntity(bill);
        }

        bill.setStatus("PAID");
        bill.setPaidDate(LocalDate.now());
        ElectricityBill saved = billRepository.save(bill);

        // 1. Create Approved Expense
        Expense exp = Expense.builder()
                .id(UUID.randomUUID().toString())
                .category("ELECTRICITY")
                .description("Common Electricity Bill for " + saved.getBillingMonth())
                .amount(saved.getAmount())
                .expenseDate(LocalDate.now())
                .vendor("State Electricity Board")
                .createdBy(payer)
                .approvalStatus("APPROVED")
                .paymentStatus("PAID")
                .approvedBy(payer)
                .approvedAt(LocalDateTime.now())
                .build();
        expenseRepository.save(exp);

        // 2. Create Immutable Ledger EXPENSE transaction
        if (!ledgerRepository.existsByReferenceTypeAndReferenceId("ELECTRICITY", saved.getId())) {
            LedgerTransaction ledgerTx = LedgerTransaction.builder()
                    .id(UUID.randomUUID().toString())
                    .type("EXPENSE")
                    .category("ELECTRICITY")
                    .amount(saved.getAmount())
                    .description("Common Electricity Bill for " + saved.getBillingMonth())
                    .referenceType("ELECTRICITY")
                    .referenceId(saved.getId())
                    .transactionDate(LocalDateTime.now())
                    .createdBy(payer)
                    .build();
            ledgerRepository.save(ledgerTx);
        }

        return ElectricityBillDto.fromEntity(saved);
    }
}
