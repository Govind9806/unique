package com.example.uniqueAproovaResidency.module.employee.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.employee.dto.EmployeeDto;
import com.example.uniqueAproovaResidency.module.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints for managing watchmen, cleaners, and maintenance staff")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get All Staff Employees")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getAllEmployees() {
        List<EmployeeDto> list = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Employee Details by ID")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployeeById(@PathVariable String id) {
        EmployeeDto dto = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER')")
    @Operation(summary = "Add New Employee")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(
            @RequestParam String name,
            @RequestParam String role,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joiningDate,
            @RequestParam BigDecimal monthlySalary) {
        EmployeeDto dto = employeeService.createEmployee(name, role, joiningDate, monthlySalary);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Employee added", dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER')")
    @Operation(summary = "Update Employee Details")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam String role,
            @RequestParam BigDecimal monthlySalary,
            @RequestParam String status) {
        EmployeeDto dto = employeeService.updateEmployee(id, name, role, monthlySalary, status);
        return ResponseEntity.ok(ApiResponse.success("Employee updated", dto));
    }
}
