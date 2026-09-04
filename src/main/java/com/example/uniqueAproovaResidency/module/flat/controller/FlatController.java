package com.example.uniqueAproovaResidency.module.flat.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.flat.dto.FlatDto;
import com.example.uniqueAproovaResidency.module.flat.service.FlatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flats")
@RequiredArgsConstructor
@Tag(name = "Flats Management", description = "Endpoints for viewing and configuring 16 flats and residents")
public class FlatController {

    private final FlatService flatService;

    @GetMapping
    @Operation(summary = "Get All 16 Flats")
    public ResponseEntity<ApiResponse<List<FlatDto>>> getAllFlats() {
        List<FlatDto> flats = flatService.getAllFlats();
        return ResponseEntity.ok(ApiResponse.success(flats));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Flat Details by ID")
    public ResponseEntity<ApiResponse<FlatDto>> getFlatById(@PathVariable String id) {
        FlatDto flat = flatService.getFlatById(id);
        return ResponseEntity.ok(ApiResponse.success(flat));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add New Flat")
    public ResponseEntity<ApiResponse<FlatDto>> createFlat(@RequestParam String flatNumber, @RequestParam Integer floor) {
        FlatDto flat = flatService.createFlat(flatNumber, floor);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Flat created", flat));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Flat Details")
    public ResponseEntity<ApiResponse<FlatDto>> updateFlat(
            @PathVariable String id,
            @RequestParam String flatNumber,
            @RequestParam Integer floor,
            @RequestParam String status) {
        FlatDto flat = flatService.updateFlat(id, flatNumber, floor, status);
        return ResponseEntity.ok(ApiResponse.success("Flat updated", flat));
    }
}
