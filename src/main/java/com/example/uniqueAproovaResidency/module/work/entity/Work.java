package com.example.uniqueAproovaResidency.module.work.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "works")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Work extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String category; // PIPELINE, PLUMBING, DRAINAGE, ELECTRICAL, WATER_TANK, PAINTING, LIFT, CCTV, GENERAL, OTHER

    private String location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_flat_id")
    private Flat responsibleFlat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_user_id")
    private User responsibleUser;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "estimated_cost", nullable = false)
    @Builder.Default
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "actual_cost")
    @Builder.Default
    private BigDecimal actualCost = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PLANNED"; // PLANNED, PENDING_APPROVAL, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(name = "photo_url")
    private String photoUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_photos", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private java.util.List<String> photoUrls = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_approval_flats", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "flat_number")
    @Builder.Default
    private java.util.List<String> approvedFlatNumbers = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_rejection_flats", joinColumns = @JoinColumn(name = "work_id"))
    @Column(name = "flat_number")
    @Builder.Default
    private java.util.List<String> rejectedFlatNumbers = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "work_vote_reasons", joinColumns = @JoinColumn(name = "work_id"))
    @MapKeyColumn(name = "flat_number")
    @Column(name = "reason")
    @Builder.Default
    private java.util.Map<String, String> flatVoteReasons = new java.util.HashMap<>();

    @Column(name = "maintenance_decision")
    private String maintenanceDecision; // ACCEPTED, REJECTED

    @Column(name = "maintenance_message")
    private String maintenanceMessage;

    @Column(name = "maintenance_responded_at")
    private java.time.LocalDateTime maintenanceRespondedAt;
}
