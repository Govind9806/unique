package com.example.uniqueAproovaResidency.module.employee.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role; // WATCHMAN, CLEANER, MAINTENANCE_WORKER, OTHER

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "monthly_salary", nullable = false)
    private BigDecimal monthlySalary;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";
}
