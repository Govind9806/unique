package com.example.uniqueAproovaResidency.module.history.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.history.dto.HistoryDto;
import com.example.uniqueAproovaResidency.module.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Tag(name = "Apartment History Log", description = "Endpoints for retrieving overall apartment activity feed (MONEY, BILLS, WATER, WORKS, MEETINGS, NOTICES, ALL)")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @Operation(summary = "Get Apartment History Feed")
    public ResponseEntity<ApiResponse<List<HistoryDto>>> getHistory(@RequestParam(required = false, defaultValue = "ALL") String category) {
        List<HistoryDto> history = historyService.getHistory(category);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
