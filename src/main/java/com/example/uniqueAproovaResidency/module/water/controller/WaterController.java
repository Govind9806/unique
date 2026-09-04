package com.example.uniqueAproovaResidency.module.water.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.document.dto.DocumentDto;
import com.example.uniqueAproovaResidency.module.document.service.DocumentService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.module.water.dto.AddWaterReadingRequest;
import com.example.uniqueAproovaResidency.module.water.dto.WaterReadingDto;
import com.example.uniqueAproovaResidency.module.water.dto.WaterSummaryDto;
import com.example.uniqueAproovaResidency.module.water.entity.WaterMeter;
import com.example.uniqueAproovaResidency.module.water.service.WaterService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
@Tag(name = "Water Management", description = "Endpoints for flat water meters, meter readings, water bills, and consumption history")
public class WaterController {

    private final WaterService waterService;
    private final DocumentService documentService;
    private final UserRepository userRepository;

    @GetMapping("/meters")
    @Operation(summary = "Get All Flat Water Meters")
    public ResponseEntity<ApiResponse<List<WaterMeter>>> getMeters() {
        log.info("REST REQUEST [GET /api/v1/water/meters]");
        List<WaterMeter> meters = waterService.getAllMeters();
        return ResponseEntity.ok(ApiResponse.success(meters));
    }

    @GetMapping("/meters/{flatId}")
    @Operation(summary = "Get Water Meter by Flat ID")
    public ResponseEntity<ApiResponse<WaterMeter>> getMeterByFlatId(@PathVariable String flatId) {
        log.info("REST REQUEST [GET /api/v1/water/meters/{}]", flatId);
        WaterMeter meter = waterService.getMeterByFlatId(flatId);
        return ResponseEntity.ok(ApiResponse.success(meter));
    }

    @PostMapping("/readings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Record New Water Meter Reading (JSON)")
    public ResponseEntity<ApiResponse<WaterReadingDto>> recordReading(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AddWaterReadingRequest request) {
        log.info("REST REQUEST [POST /api/v1/water/readings JSON] -> Flat ID: {}, Current Reading: {}", request.getFlatId(), request.getCurrentReading());
        User recorder = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        WaterReadingDto reading = waterService.recordReading(request, recorder);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Water reading recorded successfully", reading));
    }

    @PostMapping(value = "/readings/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Record New Water Meter Reading with Photo Upload (Multipart)")
    public ResponseEntity<ApiResponse<WaterReadingDto>> recordReadingWithPhoto(
            @RequestParam("flatId") String flatId,
            @RequestParam("currentReading") BigDecimal currentReading,
            @RequestParam(value = "readingDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate readingDate,
            @RequestParam("meterImage") MultipartFile meterImage,
            @AuthenticationPrincipal UserPrincipal currentUser) throws IOException {
        log.info("REST REQUEST [POST /api/v1/water/readings/upload Multipart] -> Flat ID: {}, Current Reading: {}, File: {}",
                flatId, currentReading, meterImage.getOriginalFilename());

        User recorder = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;

        // Save meter photo via DocumentService
        DocumentDto doc = documentService.uploadDocument(meterImage, "WATER_METER_PHOTO", "WATER_READING", flatId, recorder);

        AddWaterReadingRequest request = new AddWaterReadingRequest();
        request.setFlatId(flatId);
        request.setCurrentReading(currentReading);
        request.setReadingDate(readingDate != null ? readingDate : LocalDate.now());
        request.setMeterPhotoDocumentId(doc.getId());

        WaterReadingDto reading = waterService.recordReading(request, recorder);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Water reading recorded with photo proof", reading));
    }

    @GetMapping("/readings/{flatId}")
    @Operation(summary = "Get Water Readings for Flat")
    public ResponseEntity<ApiResponse<List<WaterReadingDto>>> getReadingsForFlat(@PathVariable String flatId) {
        log.info("REST REQUEST [GET /api/v1/water/readings/{}]", flatId);
        List<WaterReadingDto> list = waterService.getReadingsForFlat(flatId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/readings/{flatId}/latest")
    @Operation(summary = "Get Latest Water Reading for Flat (Read-Only Previous Reading)")
    public ResponseEntity<ApiResponse<WaterReadingDto>> getLatestReadingForFlat(@PathVariable String flatId) {
        log.info("REST REQUEST [GET /api/v1/water/readings/{}/latest]", flatId);
        WaterReadingDto dto = waterService.getLatestReadingForFlat(flatId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/readings/{flatId}/history")
    @Operation(summary = "Get Historical Water Readings for Flat")
    public ResponseEntity<ApiResponse<List<WaterReadingDto>>> getReadingHistoryForFlat(@PathVariable String flatId) {
        log.info("REST REQUEST [GET /api/v1/water/readings/{}/history]", flatId);
        List<WaterReadingDto> list = waterService.getReadingsForFlat(flatId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get Apartment Water Summary")
    public ResponseEntity<ApiResponse<WaterSummaryDto>> getWaterSummary() {
        log.info("REST REQUEST [GET /api/v1/water/summary]");
        WaterSummaryDto summary = waterService.getWaterSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
