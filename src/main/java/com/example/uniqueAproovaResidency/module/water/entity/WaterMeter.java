package com.example.uniqueAproovaResidency.module.water.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "water_meters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterMeter extends BaseEntity {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false, unique = true)
    private Flat flat;

    @Column(name = "meter_number", nullable = false, unique = true)
    private String meterNumber;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";
}
