package com.example.uniqueAproovaResidency.module.user.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.user.dto.CreateUserRequest;
import com.example.uniqueAproovaResidency.module.user.dto.ResetPasswordRequest;
import com.example.uniqueAproovaResidency.module.user.dto.UserDto;
import com.example.uniqueAproovaResidency.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing apartment residents, mobile accounts, and password resets")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER')")
    @Operation(summary = "Get All Users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        log.info("REST REQUEST [GET /api/v1/users] -> Fetching user directory");
        List<UserDto> users = userService.getAllUsers();
        log.info("REST RESPONSE [GET /api/v1/users] -> Total users found: {}", users.size());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get User by ID")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/users/{}]", id);
        UserDto user = userService.getUserById(id);
        log.info("REST RESPONSE [GET /api/v1/users/{}] -> Name: {}, Role: {}", id, user.getName(), user.getRole());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Resident Account with Mobile Number & Initial Password")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("REST REQUEST [POST /api/v1/users] -> Name: {}, Phone: {}, Role: {}, Flat: {}", request.getName(), request.getPhone(), request.getRole(), request.getFlatId());
        UserDto user = userService.createUser(request);
        log.info("REST RESPONSE [POST /api/v1/users] -> Created User ID: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User account created successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update User Details")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable String id, @Valid @RequestBody CreateUserRequest request) {
        log.info("REST REQUEST [PUT /api/v1/users/{}] -> Name: {}, Phone: {}", id, request.getName(), request.getPhone());
        UserDto user = userService.updateUser(id, request);
        log.info("REST RESPONSE [PUT /api/v1/users/{}] -> Updated successfully", id);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @PostMapping("/assign-maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign Maintenance Operational Controller Role to Flat")
    public ResponseEntity<ApiResponse<UserDto>> assignMaintenanceRole(@RequestParam String flatNumber) {
        log.info("REST REQUEST [POST /api/v1/users/assign-maintenance] -> Flat Number: {}", flatNumber);
        UserDto user = userService.assignMaintenanceRole(flatNumber);
        log.info("REST RESPONSE [POST /api/v1/users/assign-maintenance] -> Assigned Flat: {}", flatNumber);
        return ResponseEntity.ok(ApiResponse.success("Maintenance operational role assigned successfully", user));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin Reset Password for Resident Mobile Number")
    public ResponseEntity<ApiResponse<UserDto>> resetPassword(
            @PathVariable String id,
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("REST REQUEST [POST /api/v1/users/{}/reset-password]", id);
        UserDto user = userService.resetUserPassword(id, request.getNewPassword());
        log.info("REST RESPONSE [POST /api/v1/users/{}/reset-password] -> Password reset completed", id);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully for mobile account", user));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update User Status (ACTIVE/INACTIVE)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(@PathVariable String id, @RequestParam String status) {
        log.info("REST REQUEST [PATCH /api/v1/users/{}/status] -> New Status: {}", id, status);
        UserDto user = userService.updateUserStatus(id, status);
        log.info("REST RESPONSE [PATCH /api/v1/users/{}/status] -> Status set to {}", id, status);
        return ResponseEntity.ok(ApiResponse.success("User status updated", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Resident User Account (Admin Only)")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String id) {
        log.info("REST REQUEST [DELETE /api/v1/users/{}]", id);
        userService.deleteUser(id);
        log.info("REST RESPONSE [DELETE /api/v1/users/{}] -> Deleted successfully", id);
        return ResponseEntity.ok(ApiResponse.success("User account deleted successfully", id));
    }
}
