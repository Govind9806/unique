package com.example.uniqueAproovaResidency.module.dashboard.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.dashboard.dto.AdminDashboardDto;
import com.example.uniqueAproovaResidency.module.dashboard.dto.ResidentDashboardDto;
import com.example.uniqueAproovaResidency.module.dashboard.service.DashboardService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Aggregated Mobile Dashboards", description = "Aggregated APIs for loading Figma Resident Home and Admin Home screens in a single network request")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resident")
    @Operation(summary = "Get Resident Home Screen Dashboard Payload")
    public ResponseEntity<ApiResponse<ResidentDashboardDto>> getResidentDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        ResidentDashboardDto dto = dashboardService.getResidentDashboard(currentUser);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Get Admin / Maintenance Dashboard Payload")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getAdminDashboard() {
        AdminDashboardDto dto = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
