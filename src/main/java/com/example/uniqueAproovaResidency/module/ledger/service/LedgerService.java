package com.example.uniqueAproovaResidency.module.ledger.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.finance.service.FinanceService;
import com.example.uniqueAproovaResidency.module.ledger.dto.BalanceAdjustmentLogDto;
import com.example.uniqueAproovaResidency.module.ledger.dto.BalanceAdjustmentRequest;
import com.example.uniqueAproovaResidency.module.ledger.dto.LedgerTransactionDto;
import com.example.uniqueAproovaResidency.module.ledger.entity.BalanceAdjustmentLog;
import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import com.example.uniqueAproovaResidency.module.ledger.repository.BalanceAdjustmentLogRepository;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.uniqueAproovaResidency.module.expense.repository.ExpenseRepository;
import com.example.uniqueAproovaResidency.module.work.repository.WorkRepository;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final BalanceAdjustmentLogRepository balanceAdjustmentLogRepository;
    private final FinanceService financeService;
    private final ExpenseRepository expenseRepository;
    private final WorkRepository workRepository;

    @Transactional(readOnly = true)
    public List<LedgerTransactionDto> getAllLedgerTransactions() {
        return ledgerRepository.findAllByOrderByTransactionDateDesc().stream()
                .map(tx -> {
                    LedgerTransactionDto dto = LedgerTransactionDto.fromEntity(tx);
                    if ((dto.getPhotoUrl() == null || dto.getPhotoUrl().isEmpty()) && tx.getReferenceId() != null) {
                        if ("EXPENSE".equalsIgnoreCase(tx.getReferenceType())) {
                            expenseRepository.findById(tx.getReferenceId()).ifPresent(exp -> {
                                String photo = exp.getPhotoUrl();
                                if ((photo == null || photo.isEmpty()) && exp.getReceiptPhotoDocumentId() != null) {
                                    photo = "/api/v1/documents/" + exp.getReceiptPhotoDocumentId() + "/file";
                                }
                                dto.setPhotoUrl(photo);
                                dto.setReceiptPhotoDocumentId(exp.getReceiptPhotoDocumentId());
                                if (dto.getVendor() == null || dto.getVendor().isEmpty()) {
                                    dto.setVendor(exp.getVendor());
                                }
                            });
                        } else if ("WORK".equalsIgnoreCase(tx.getReferenceType())) {
                            workRepository.findById(tx.getReferenceId()).ifPresent(wk -> {
                                dto.setPhotoUrl(wk.getPhotoUrl());
                                if (dto.getVendor() == null || dto.getVendor().isEmpty()) {
                                    dto.setVendor(wk.getVendorName());
                                }
                            });
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LedgerTransactionDto getLedgerTransactionById(String id) {
        LedgerTransaction tx = ledgerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LedgerTransaction", "id", id));
        LedgerTransactionDto dto = LedgerTransactionDto.fromEntity(tx);
        if ((dto.getPhotoUrl() == null || dto.getPhotoUrl().isEmpty()) && tx.getReferenceId() != null) {
            if ("EXPENSE".equalsIgnoreCase(tx.getReferenceType())) {
                expenseRepository.findById(tx.getReferenceId()).ifPresent(exp -> {
                    String photo = exp.getPhotoUrl();
                    if ((photo == null || photo.isEmpty()) && exp.getReceiptPhotoDocumentId() != null) {
                        photo = "/api/v1/documents/" + exp.getReceiptPhotoDocumentId() + "/file";
                    }
                    dto.setPhotoUrl(photo);
                    dto.setReceiptPhotoDocumentId(exp.getReceiptPhotoDocumentId());
                    if (dto.getVendor() == null || dto.getVendor().isEmpty()) {
                        dto.setVendor(exp.getVendor());
                    }
                });
            } else if ("WORK".equalsIgnoreCase(tx.getReferenceType())) {
                workRepository.findById(tx.getReferenceId()).ifPresent(wk -> {
                    dto.setPhotoUrl(wk.getPhotoUrl());
                    if (dto.getVendor() == null || dto.getVendor().isEmpty()) {
                        dto.setVendor(wk.getVendorName());
                    }
                });
            }
        }
        return dto;
    }

    // Manual Balance Adjustment with mandatory Reason & Audit Trail
    @Transactional
    public BalanceAdjustmentLogDto adjustBalance(BalanceAdjustmentRequest request, User modifier) {
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Reason for balance modification is mandatory.");
        }

        BigDecimal currentBal = financeService.getSummary().getCurrentBalance();
        BigDecimal newBal = request.getNewBalance();
        BigDecimal difference = newBal.subtract(currentBal);

        // 1. Log Audit Record
        BalanceAdjustmentLog audit = BalanceAdjustmentLog.builder()
                .id(UUID.randomUUID().toString())
                .previousBalance(currentBal)
                .newBalance(newBal)
                .difference(difference)
                .reason(request.getReason().trim())
                .modifiedBy(modifier)
                .modifiedAt(LocalDateTime.now())
                .build();
        balanceAdjustmentLogRepository.save(audit);

        // 2. Create Ledger Transaction to balance financial ledger
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            String type = difference.compareTo(BigDecimal.ZERO) > 0 ? "INCOME" : "EXPENSE";
            BigDecimal amt = difference.abs();

            LedgerTransaction tx = LedgerTransaction.builder()
                    .id(UUID.randomUUID().toString())
                    .type(type)
                    .category("BALANCE_ADJUSTMENT")
                    .amount(amt)
                    .description("Balance Adjustment (" + type + "): " + request.getReason().trim())
                    .referenceType("ADJUSTMENT")
                    .transactionDate(LocalDateTime.now())
                    .createdBy(modifier)
                    .build();
            ledgerRepository.save(tx);
        }

        return BalanceAdjustmentLogDto.builder()
                .id(audit.getId())
                .previousBalance(audit.getPreviousBalance())
                .newBalance(audit.getNewBalance())
                .difference(audit.getDifference())
                .reason(audit.getReason())
                .modifiedByUserId(modifier != null ? modifier.getId() : null)
                .modifiedByName(modifier != null ? modifier.getName() : "System")
                .modifiedAt(audit.getModifiedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BalanceAdjustmentLogDto> getBalanceAdjustmentHistory() {
        return balanceAdjustmentLogRepository.findAllByOrderByModifiedAtDesc().stream()
                .map(a -> BalanceAdjustmentLogDto.builder()
                        .id(a.getId())
                        .previousBalance(a.getPreviousBalance())
                        .newBalance(a.getNewBalance())
                        .difference(a.getDifference())
                        .reason(a.getReason())
                        .modifiedByUserId(a.getModifiedBy() != null ? a.getModifiedBy().getId() : null)
                        .modifiedByName(a.getModifiedBy() != null ? a.getModifiedBy().getName() : "System")
                        .modifiedAt(a.getModifiedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
