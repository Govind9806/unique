package com.example.uniqueAproovaResidency.module.search.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Global Search", description = "Endpoints for searching flats, residents, payments, expenses, works, and notices")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Global Search Query")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(@RequestParam("q") String query) {
        Map<String, Object> results = searchService.globalSearch(query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
