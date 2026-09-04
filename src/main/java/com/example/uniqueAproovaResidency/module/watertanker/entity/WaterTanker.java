package com.example.uniqueAproovaResidency.module.watertanker.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "water_tankers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterTanker extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String supplier;

    @Column(name = "tanker_date", nullable = false)
    private LocalDate tankerDate;

    @Column(name = "quantity_liters", nullable = false)
    private Integer quantityLiters;

    @Column(nullable = false)
    private BigDecimal amount;

    private String reason;

    @Column(name = "invoice_document_id")
    private String invoiceDocumentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id")
    private User recordedBy;

    @Column(name = "approval_status", nullable = false)
    @Builder.Default
    private String approvalStatus = "APPROVED";

    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private String paymentStatus = "PAID";
}
