package com.example.uniqueAproovaResidency.module.expense.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.work.entity.Work;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String category; // WATER, ELECTRICITY, REPAIR, SALARY, TANKER, MAINTENANCE, OTHER

    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    private String vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_work_id")
    private Work relatedWork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_flat_id")
    private Flat responsibleFlat;

    @Column(name = "approval_status", nullable = false)
    @Builder.Default
    private String approvalStatus = "PENDING_APPROVAL"; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, CANCELLED

    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private String paymentStatus = "UNPAID"; // UNPAID, PAID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "is_emergency")
    @Builder.Default
    private Boolean isEmergency = false;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "receipt_photo_document_id")
    private String receiptPhotoDocumentId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expense_approval_flats", joinColumns = @JoinColumn(name = "expense_id"))
    @Column(name = "flat_number")
    @Builder.Default
    private List<String> approvedFlatNumbers = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expense_rejection_flats", joinColumns = @JoinColumn(name = "expense_id"))
    @Column(name = "flat_number")
    @Builder.Default
    private List<String> rejectedFlatNumbers = new java.util.ArrayList<>();
}
