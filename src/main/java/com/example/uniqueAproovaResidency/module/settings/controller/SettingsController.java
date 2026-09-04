package com.example.uniqueAproovaResidency.module.settings.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.settings.entity.ApartmentSettings;
import com.example.uniqueAproovaResidency.module.settings.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings Management", description = "Endpoints for configuring maintenance amounts, water rates, due dates, and language preferences")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "Get Apartment Settings")
    public ResponseEntity<ApiResponse<ApartmentSettings>> getSettings() {
        ApartmentSettings settings = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Apartment Settings")
    public ResponseEntity<ApiResponse<ApartmentSettings>> updateSettings(@RequestBody ApartmentSettings updated) {
        ApartmentSettings settings = settingsService.updateSettings(updated);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", settings));
    }
}
