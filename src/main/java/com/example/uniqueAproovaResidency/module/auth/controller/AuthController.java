package com.example.uniqueAproovaResidency.module.auth.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.auth.dto.ChangePasswordRequest;
import com.example.uniqueAproovaResidency.module.auth.dto.LoginRequest;
import com.example.uniqueAproovaResidency.module.auth.dto.LoginResponse;
import com.example.uniqueAproovaResidency.module.auth.service.AuthService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, logout, and credential management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User Login via Mobile Number or Email + Password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST REQUEST [POST /api/v1/auth/login] -> Identifier: {}", request.getPhone() != null ? request.getPhone() : request.getEmail());
        LoginResponse response = authService.login(request);
        log.info("REST RESPONSE [POST /api/v1/auth/login] -> User ID: {}, Name: {}, Role: {}", response.getUserId(), response.getName(), response.getRole());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User Logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("REST REQUEST [POST /api/v1/auth/logout]");
        return ResponseEntity.ok(ApiResponse.success("Logout successful", "Logged out"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Secure Resident Password Change")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("REST REQUEST [POST /api/v1/auth/change-password] -> User ID: {}", currentUser != null ? currentUser.getId() : "null");
        authService.changePassword(currentUser.getId(), request, currentUser);
        log.info("REST RESPONSE [POST /api/v1/auth/change-password] -> Password updated successfully for User ID: {}", currentUser != null ? currentUser.getId() : "null");
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", "Updated"));
    }
}
