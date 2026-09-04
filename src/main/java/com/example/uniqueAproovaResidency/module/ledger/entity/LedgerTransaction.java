package com.example.uniqueAproovaResidency.module.ledger.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerTransaction extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String type; // INCOME, EXPENSE, ADJUSTMENT

    @Column(nullable = false)
    private String category; // MAINTENANCE_FEE, WATER_BILL, PIPELINE_REPAIR, TANKER, ELECTRICITY, WATCHMAN_SALARY, CLEANER_SALARY, OTHER

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Column(name = "reference_type")
    private String referenceType; // PAYMENT, EXPENSE, ADJUSTMENT, SALARY, ELECTRICITY

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "receipt_photo_document_id")
    private String receiptPhotoDocumentId;

    @Column(name = "vendor")
    private String vendor;
}
