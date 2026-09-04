package com.example.uniqueAproovaResidency.module.maintenance.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_bills", uniqueConstraints = {
        @UniqueConstraint(name = "uk_flat_month_year", columnNames = {"flat_id", "billing_month", "billing_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceBill extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED
}
