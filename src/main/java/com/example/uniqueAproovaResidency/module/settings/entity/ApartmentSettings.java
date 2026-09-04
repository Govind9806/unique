package com.example.uniqueAproovaResidency.module.settings.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "apartment_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentSettings extends BaseEntity {

    @Id
    private String id;

    @Column(name = "maintenance_amount", nullable = false)
    @Builder.Default
    private BigDecimal maintenanceAmount = BigDecimal.valueOf(2000);

    @Column(name = "water_rate_per_unit", nullable = false)
    @Builder.Default
    private BigDecimal waterRatePerUnit = BigDecimal.valueOf(40);

    @Column(name = "due_day_of_month", nullable = false)
    @Builder.Default
    private Integer dueDayOfMonth = 10;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = true;
}
