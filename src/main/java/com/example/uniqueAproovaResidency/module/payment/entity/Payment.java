package com.example.uniqueAproovaResidency.module.payment.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private MaintenanceBill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // UPI_INTENT, ONLINE_PAYMENT, OFFLINE_UPI, CASH, BANK_TRANSFER

    @Column(nullable = false)
    @Builder.Default
    private String provider = "DIRECT_UPI"; // DIRECT_UPI, RAZORPAY_HISTORICAL, BANK_PSP

    @Column(name = "gateway_order_id")
    private String gatewayOrderId; // Merchant Reference / Internal Reference

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId; // Provider Transaction ID / Bank UTR

    @Column(name = "transaction_reference")
    private String transactionReference; // Merchant Transaction Ref / UTR

    @Column(nullable = false)
    @Builder.Default
    private String status = "INITIATED"; // INITIATED, PENDING, SUCCESS, FAILED, REFUNDED, CANCELLED, PENDING_VERIFICATION

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
