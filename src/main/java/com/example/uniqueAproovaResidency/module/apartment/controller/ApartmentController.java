package com.example.uniqueAproovaResidency.module.apartment.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.apartment.entity.Apartment;
import com.example.uniqueAproovaResidency.module.apartment.service.ApartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apartment")
@RequiredArgsConstructor
@Tag(name = "Apartment Configuration", description = "Endpoints for apartment residency details and rules")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @GetMapping
    @Operation(summary = "Get Apartment Information")
    public ResponseEntity<ApiResponse<Apartment>> getApartmentInfo() {
        Apartment info = apartmentService.getApartmentInfo();
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Apartment Information")
    public ResponseEntity<ApiResponse<Apartment>> updateApartmentInfo(@RequestBody Apartment updated) {
        Apartment info = apartmentService.updateApartmentInfo(updated);
        return ResponseEntity.ok(ApiResponse.success("Apartment updated successfully", info));
    }
}
