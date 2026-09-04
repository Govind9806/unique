package com.example.uniqueAproovaResidency.module.report.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.finance.dto.FinanceSummaryDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.ApartmentBillStatusDto;
import com.example.uniqueAproovaResidency.module.report.service.ReportService;
import com.example.uniqueAproovaResidency.module.water.dto.WaterSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics", description = "Endpoints for generating financial, collection, water, work, and salary analytics reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/financial")
    @Operation(summary = "Get Monthly Financial Report")
    public ResponseEntity<ApiResponse<FinanceSummaryDto>> getFinancialReport() {
        FinanceSummaryDto report = reportService.getFinancialReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/maintenance")
    @Operation(summary = "Get Maintenance Fee Collection Report")
    public ResponseEntity<ApiResponse<ApartmentBillStatusDto>> getMaintenanceReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        ApartmentBillStatusDto report = reportService.getMaintenanceCollectionReport(month, year);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/water")
    @Operation(summary = "Get Water Consumption Report")
    public ResponseEntity<ApiResponse<WaterSummaryDto>> getWaterReport() {
        WaterSummaryDto report = reportService.getWaterReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
