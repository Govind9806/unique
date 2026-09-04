package com.example.uniqueAproovaResidency.module.employee.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.employee.dto.EmployeeDto;
import com.example.uniqueAproovaResidency.module.employee.entity.Employee;
import com.example.uniqueAproovaResidency.module.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return EmployeeDto.fromEntity(employee);
    }

    @Transactional
    public EmployeeDto createEmployee(String name, String role, LocalDate joiningDate, BigDecimal monthlySalary) {
        Employee emp = Employee.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .role(role.toUpperCase())
                .joiningDate(joiningDate != null ? joiningDate : LocalDate.now())
                .monthlySalary(monthlySalary)
                .status("ACTIVE")
                .build();

        Employee saved = employeeRepository.save(emp);
        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto updateEmployee(String id, String name, String role, BigDecimal monthlySalary, String status) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        emp.setName(name);
        emp.setRole(role.toUpperCase());
        emp.setMonthlySalary(monthlySalary);
        emp.setStatus(status);

        Employee saved = employeeRepository.save(emp);
        return EmployeeDto.fromEntity(saved);
    }
}
