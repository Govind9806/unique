package com.example.uniqueAproovaResidency.module.watertanker.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.module.watertanker.dto.CreateWaterTankerRequest;
import com.example.uniqueAproovaResidency.module.watertanker.dto.WaterTankerDto;
import com.example.uniqueAproovaResidency.module.watertanker.service.WaterTankerService;
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
@RequestMapping("/api/v1/water-tankers")
@RequiredArgsConstructor
@Tag(name = "Water Tanker", description = "Endpoints for recording apartment water tanker orders, proof documents, and costs")
public class WaterTankerController {

    private final WaterTankerService tankerService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Water Tanker Entries")
    public ResponseEntity<ApiResponse<List<WaterTankerDto>>> getAllTankers() {
        List<WaterTankerDto> tankers = tankerService.getAllTankers();
        return ResponseEntity.ok(ApiResponse.success(tankers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Water Tanker Details by ID")
    public ResponseEntity<ApiResponse<WaterTankerDto>> getTankerById(@PathVariable String id) {
        WaterTankerDto tanker = tankerService.getTankerById(id);
        return ResponseEntity.ok(ApiResponse.success(tanker));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_MEMBER') or hasRole('TREASURER')")
    @Operation(summary = "Add New Water Tanker Delivery")
    public ResponseEntity<ApiResponse<WaterTankerDto>> addTanker(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateWaterTankerRequest request) {
        User recorder = userRepository.findById(currentUser.getId()).orElse(null);
        WaterTankerDto dto = tankerService.addTanker(request, recorder);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Water tanker entry created", dto));
    }
}
