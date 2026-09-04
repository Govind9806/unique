package com.example.uniqueAproovaResidency.module.employee.dto;

import com.example.uniqueAproovaResidency.module.employee.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private String id;
    private String name;
    private String role;
    private LocalDate joiningDate;
    private BigDecimal monthlySalary;
    private String status;

    public static EmployeeDto fromEntity(Employee employee) {
        return EmployeeDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .role(employee.getRole())
                .joiningDate(employee.getJoiningDate())
                .monthlySalary(employee.getMonthlySalary())
                .status(employee.getStatus())
                .build();
    }
}
