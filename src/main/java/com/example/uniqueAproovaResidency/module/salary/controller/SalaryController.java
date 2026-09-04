package com.example.uniqueAproovaResidency.module.salary.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.salary.dto.CreateSalaryRecordRequest;
import com.example.uniqueAproovaResidency.module.salary.dto.SalaryRecordDto;
import com.example.uniqueAproovaResidency.module.salary.service.SalaryService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
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
@RequestMapping("/api/v1/salaries")
@RequiredArgsConstructor
@Tag(name = "Staff Salary Management", description = "Endpoints for staff salary calculation, advances, deductions, bonuses, and payments")
public class SalaryController {

    private final SalaryService salaryService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Salary Records")
    public ResponseEntity<ApiResponse<List<SalaryRecordDto>>> getAllSalaries() {
        List<SalaryRecordDto> salaries = salaryService.getAllSalaries();
        return ResponseEntity.ok(ApiResponse.success(salaries));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Calculate and Create Monthly Salary Record")
    public ResponseEntity<ApiResponse<SalaryRecordDto>> calculateSalary(@Valid @RequestBody CreateSalaryRecordRequest request) {
        SalaryRecordDto dto = salaryService.calculateAndCreateSalary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Salary record created", dto));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Mark Salary Paid (Triggers Expense & Ledger EXPENSE Entry)")
    public ResponseEntity<ApiResponse<SalaryRecordDto>> paySalary(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User payer = userRepository.findById(currentUser.getId()).orElse(null);
        SalaryRecordDto dto = salaryService.paySalary(id, payer);
        return ResponseEntity.ok(ApiResponse.success("Salary paid and logged in ledger", dto));
    }
}
