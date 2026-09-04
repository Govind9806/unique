package com.example.uniqueAproovaResidency.module.maintenance.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.maintenance.dto.ApartmentBillStatusDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.BillDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.FlatDuesSummaryDto;
import com.example.uniqueAproovaResidency.module.maintenance.service.BillService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Tag(name = "Maintenance Billing", description = "Endpoints for monthly flat maintenance bill generation and status tracking")
public class BillController {

    private final BillService billService;

    @GetMapping("/my")
    @Operation(summary = "Get Current Resident's Bills")
    public ResponseEntity<ApiResponse<List<BillDto>>> getMyBills(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [GET /api/v1/bills/my] -> User Flat ID: {}", currentUser.getFlatId());
        List<BillDto> bills = billService.getBillsForFlat(currentUser.getFlatId());
        return ResponseEntity.ok(ApiResponse.success(bills));
    }

    @GetMapping("/my/pending")
    @Operation(summary = "Get Current Resident's Pending Bills")
    public ResponseEntity<ApiResponse<List<BillDto>>> getMyPendingBills(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [GET /api/v1/bills/my/pending] -> User Flat ID: {}", currentUser.getFlatId());
        List<BillDto> bills = billService.getPendingBillsForFlat(currentUser.getFlatId());
        return ResponseEntity.ok(ApiResponse.success(bills));
    }

    @GetMapping("/apartment-status")
    @Operation(summary = "Get Apartment-Wide Maintenance Payment Status (All 16 Flats)")
    public ResponseEntity<ApiResponse<ApartmentBillStatusDto>> getApartmentBillStatus(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        log.info("REST REQUEST [GET /api/v1/bills/apartment-status] -> month: {}, year: {}", month, year);
        ApartmentBillStatusDto status = billService.getApartmentBillStatus(month, year);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/flat/{flatNumber}/dues")
    @Operation(summary = "Get Authoritative Real-Time Dues Breakdown for a Specific Flat")
    public ResponseEntity<ApiResponse<FlatDuesSummaryDto>> getFlatDuesSummary(@PathVariable String flatNumber) {
        log.info("REST REQUEST [GET /api/v1/bills/flat/{}/dues]", flatNumber);
        FlatDuesSummaryDto summary = billService.getFlatDuesSummary(flatNumber);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Bill Details by ID")
    public ResponseEntity<ApiResponse<BillDto>> getBillById(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/bills/{}]", id);
        BillDto bill = billService.getBillById(id);
        return ResponseEntity.ok(ApiResponse.success(bill));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_FLAT')")
    @Operation(summary = "Generate Monthly Maintenance Bills for All 16 Flats")
    public ResponseEntity<ApiResponse<List<BillDto>>> generateMonthlyBills(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam BigDecimal amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        log.info("REST REQUEST [POST /api/v1/bills/generate] -> month: {}, year: {}, amount: {}", month, year, amount);
        List<BillDto> bills = billService.generateMonthlyBills(month, year, amount, dueDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Monthly bills generated", bills));
    }
}
