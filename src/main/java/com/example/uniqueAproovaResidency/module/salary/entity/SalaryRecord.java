package com.example.uniqueAproovaResidency.module.salary.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salary_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryRecord extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "month_year", nullable = false)
    private String monthYear;

    @Column(name = "base_salary", nullable = false)
    private BigDecimal baseSalary;

    @Builder.Default
    private BigDecimal advance = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal deduction = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID

    @Column(name = "proof_document_id")
    private String proofDocumentId;
}
