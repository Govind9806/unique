package com.example.uniqueAproovaResidency.module.apartment.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(name = "total_flats", nullable = false)
    private Integer totalFlats;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String timezone;

    @Column(name = "opening_balance", nullable = false)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;
}
