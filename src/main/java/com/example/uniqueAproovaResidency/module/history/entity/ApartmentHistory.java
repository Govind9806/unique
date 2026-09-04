package com.example.uniqueAproovaResidency.module.history.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "apartment_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentHistory extends BaseEntity {

    @Id
    private String id;

    @Column(name = "event_category", nullable = false)
    private String eventCategory; // MONEY, BILLS, WATER, WORKS, MEETINGS, NOTICES, ALL

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private String referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
}
