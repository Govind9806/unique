package com.example.uniqueAproovaResidency.module.appversion.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.appversion.entity.AppVersion;
import com.example.uniqueAproovaResidency.module.appversion.service.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
@Tag(name = "App Version Check", description = "Public endpoint for Flutter APK version update checking")
public class AppVersionController {

    private final AppVersionService appVersionService;

    @GetMapping("/version")
    @Operation(summary = "Get Latest App Version and APK Download URL")
    public ResponseEntity<ApiResponse<AppVersion>> getAppVersion() {
        AppVersion version = appVersionService.getLatestAppVersion();
        return ResponseEntity.ok(ApiResponse.success(version));
    }
}
