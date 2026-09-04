package com.example.uniqueAproovaResidency.module.water.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "water_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterBill extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_id")
    private WaterReading reading;

    @Column(name = "previous_reading", nullable = false)
    private BigDecimal previousReading;

    @Column(name = "current_reading", nullable = false)
    private BigDecimal currentReading;

    @Column(nullable = false)
    private BigDecimal consumption;

    @Column(name = "rate_per_unit", nullable = false)
    private BigDecimal ratePerUnit;

    @Column(name = "fixed_charge", nullable = false)
    private BigDecimal fixedCharge;

    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount;

    @Column(name = "billing_period", nullable = false)
    private String billingPeriod;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";
}
