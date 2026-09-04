package com.example.uniqueAproovaResidency.module.approval.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.expense.dto.ExpenseDto;
import com.example.uniqueAproovaResidency.module.expense.service.ExpenseService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Expense Approval Workflow", description = "Workflow for reviewing, approving, and rejecting apartment expenses across flat members")
public class ApprovalController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_FLAT') or hasRole('FLAT_MEMBER')")
    @Operation(summary = "Get Pending Expense Approvals (Broadcasting to All Flat Members)")
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getPendingApprovals() {
        log.info("REST REQUEST [GET /api/v1/approvals/pending] -> Admin/Member monitoring pending reviews");
        List<ExpenseDto> pending = expenseService.getPendingApprovals();
        log.info("REST RESPONSE [GET /api/v1/approvals/pending] -> Total pending expense approvals: {}", pending.size());
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('FLAT_MEMBER')")
    @Operation(summary = "Approve Expense (Flat Members Only - Creates Ledger EXPENSE Transaction)")
    public ResponseEntity<ApiResponse<ExpenseDto>> approveExpense(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/approvals/{}/approve] -> User ID: {}", id, currentUser.getId());
        User approver = userRepository.findById(currentUser.getId()).orElse(null);
        ExpenseDto dto = expenseService.approveExpense(id, approver);
        log.info("REST RESPONSE [POST /api/v1/approvals/{}/approve] -> Approved successfully", id);
        return ResponseEntity.ok(ApiResponse.success("Expense approved and recorded in ledger", dto));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('FLAT_MEMBER')")
    @Operation(summary = "Reject Expense with Reason (Flat Members Only)")
    public ResponseEntity<ApiResponse<ExpenseDto>> rejectExpense(
            @PathVariable String id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/approvals/{}/reject] -> User ID: {}, Reason: {}", id, currentUser.getId(), reason);
        User rejecter = userRepository.findById(currentUser.getId()).orElse(null);
        ExpenseDto dto = expenseService.rejectExpense(id, reason, rejecter);
        log.info("REST RESPONSE [POST /api/v1/approvals/{}/reject] -> Rejected successfully", id);
        return ResponseEntity.ok(ApiResponse.success("Expense rejected", dto));
    }
}
