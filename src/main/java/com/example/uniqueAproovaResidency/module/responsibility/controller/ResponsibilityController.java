package com.example.uniqueAproovaResidency.module.responsibility.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.responsibility.dto.CreateResponsibilityRequest;
import com.example.uniqueAproovaResidency.module.responsibility.dto.ResponsibilityDto;
import com.example.uniqueAproovaResidency.module.responsibility.service.ResponsibilityService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/responsibilities")
@RequiredArgsConstructor
@Tag(name = "Responsibility Management", description = "Endpoints for flat responsibility assignments (Maintenance flat, Water flat, etc.)")
public class ResponsibilityController {

    private final ResponsibilityService responsibilityService;

    @GetMapping
    @Operation(summary = "Get Current Active Responsibilities")
    public ResponseEntity<ApiResponse<List<ResponsibilityDto>>> getCurrentResponsibilities() {
        List<ResponsibilityDto> list = responsibilityService.getCurrentResponsibilities();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{type}")
    @Operation(summary = "Get Current Active Responsibility by Type")
    public ResponseEntity<ApiResponse<ResponsibilityDto>> getResponsibilityByType(@PathVariable String type) {
        ResponsibilityDto resp = responsibilityService.getActiveResponsibilityByType(type);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign Responsibility to Flat")
    public ResponseEntity<ApiResponse<ResponsibilityDto>> assignResponsibility(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateResponsibilityRequest request) {
        ResponsibilityDto resp = responsibilityService.assignResponsibility(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Responsibility assigned", resp));
    }

    @GetMapping("/history")
    @Operation(summary = "Get Complete Responsibility Change History")
    public ResponseEntity<ApiResponse<List<ResponsibilityDto>>> getResponsibilityHistory() {
        List<ResponsibilityDto> history = responsibilityService.getResponsibilityHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
