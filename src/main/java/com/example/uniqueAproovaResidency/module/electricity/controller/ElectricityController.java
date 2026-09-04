package com.example.uniqueAproovaResidency.module.electricity.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.electricity.dto.ElectricityBillDto;
import com.example.uniqueAproovaResidency.module.electricity.service.ElectricityService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/electricity")
@RequiredArgsConstructor
@Tag(name = "Electricity Management", description = "Endpoints for managing common apartment electricity bills and payments")
public class ElectricityController {

    private final ElectricityService electricityService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Electricity Bills")
    public ResponseEntity<ApiResponse<List<ElectricityBillDto>>> getAllBills() {
        List<ElectricityBillDto> bills = electricityService.getAllElectricityBills();
        return ResponseEntity.ok(ApiResponse.success(bills));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Electricity Bill Details by ID")
    public ResponseEntity<ApiResponse<ElectricityBillDto>> getBillById(@PathVariable String id) {
        ElectricityBillDto bill = electricityService.getElectricityBillById(id);
        return ResponseEntity.ok(ApiResponse.success(bill));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Add Electricity Bill")
    public ResponseEntity<ApiResponse<ElectricityBillDto>> createBill(
            @RequestParam String billingMonth,
            @RequestParam BigDecimal amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(required = false) String documentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User creator = userRepository.findById(currentUser.getId()).orElse(null);
        ElectricityBillDto dto = electricityService.createElectricityBill(billingMonth, amount, dueDate, documentId, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Electricity bill added", dto));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Pay Electricity Bill (Triggers Expense & Ledger EXPENSE Entry)")
    public ResponseEntity<ApiResponse<ElectricityBillDto>> payBill(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User payer = userRepository.findById(currentUser.getId()).orElse(null);
        ElectricityBillDto dto = electricityService.payElectricityBill(id, payer);
        return ResponseEntity.ok(ApiResponse.success("Electricity bill paid and logged in ledger", dto));
    }
}
