package com.example.uniqueAproovaResidency.module.flat.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flat extends BaseEntity {

    @Id
    private String id;

    @Column(name = "flat_number", nullable = false, unique = true)
    private String flatNumber;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    @Builder.Default
    private String status = "OCCUPIED";
}
