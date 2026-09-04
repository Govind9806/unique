package com.example.uniqueAproovaResidency.module.water.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "water_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterReading extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private WaterMeter meter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Column(name = "previous_reading")
    private BigDecimal previousReading;

    @Column(name = "current_reading", nullable = false)
    private BigDecimal currentReading;

    @Column(nullable = false)
    private BigDecimal consumption;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "reading_type", nullable = false)
    @Builder.Default
    private String readingType = "REGULAR"; // INITIAL, REGULAR

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id")
    private User recordedBy;

    @Column(name = "meter_photo_document_id", nullable = false)
    private String meterPhotoDocumentId;
}
