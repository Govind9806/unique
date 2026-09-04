package com.example.uniqueAproovaResidency.module.ledger.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.ledger.dto.BalanceAdjustmentLogDto;
import com.example.uniqueAproovaResidency.module.ledger.dto.BalanceAdjustmentRequest;
import com.example.uniqueAproovaResidency.module.ledger.dto.LedgerTransactionDto;
import com.example.uniqueAproovaResidency.module.ledger.service.LedgerService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Tag(name = "Financial Ledger", description = "Endpoints for immutable double-entry income and expense audit ledger and balance adjustments")
public class LedgerController {

    private final LedgerService ledgerService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Financial Ledger Transactions")
    public ResponseEntity<ApiResponse<List<LedgerTransactionDto>>> getLedger() {
        log.info("REST REQUEST [GET /api/v1/ledger]");
        List<LedgerTransactionDto> list = ledgerService.getAllLedgerTransactions();
        log.info("REST RESPONSE [GET /api/v1/ledger] -> Total ledger transactions: {}", list.size());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Ledger Transaction Details by ID")
    public ResponseEntity<ApiResponse<LedgerTransactionDto>> getLedgerById(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/ledger/{}]", id);
        LedgerTransactionDto dto = ledgerService.getLedgerTransactionById(id);
        log.info("REST RESPONSE [GET /api/v1/ledger/{}] -> Description: {}, Amount: {}", id, dto.getDescription(), dto.getAmount());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/balance-adjustment")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Adjust Society Balance with Mandatory Audit Reason (Maintenance Role Only)")
    public ResponseEntity<ApiResponse<BalanceAdjustmentLogDto>> adjustBalance(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody BalanceAdjustmentRequest request) {
        log.info("REST REQUEST [POST /api/v1/ledger/balance-adjustment] -> New Balance: ₹{}, Mandatory Reason: '{}', ModifiedBy: {}", request.getNewBalance(), request.getReason(), currentUser != null ? currentUser.getId() : "null");
        User modifier = userRepository.findById(currentUser.getId()).orElse(null);
        BalanceAdjustmentLogDto dto = ledgerService.adjustBalance(request, modifier);
        log.info("REST RESPONSE [POST /api/v1/ledger/balance-adjustment] -> Audit Log ID: {}, Previous: ₹{}, New: ₹{}, Difference: ₹{}", dto.getId(), dto.getPreviousBalance(), dto.getNewBalance(), dto.getDifference());
        return ResponseEntity.ok(ApiResponse.success("Balance adjusted successfully and recorded in audit log", dto));
    }

    @GetMapping("/balance-adjustments")
    @Operation(summary = "Get Balance Modification Audit History")
    public ResponseEntity<ApiResponse<List<BalanceAdjustmentLogDto>>> getBalanceAdjustmentHistory() {
        log.info("REST REQUEST [GET /api/v1/ledger/balance-adjustments]");
        List<BalanceAdjustmentLogDto> list = ledgerService.getBalanceAdjustmentHistory();
        log.info("REST RESPONSE [GET /api/v1/ledger/balance-adjustments] -> Total balance audit records: {}", list.size());
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
