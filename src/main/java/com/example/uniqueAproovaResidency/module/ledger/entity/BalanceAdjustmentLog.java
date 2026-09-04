package com.example.uniqueAproovaResidency.module.ledger.entity;

import com.example.uniqueAproovaResidency.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_adjustment_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceAdjustmentLog {

    @Id
    private String id;

    @Column(name = "previous_balance", nullable = false)
    private BigDecimal previousBalance;

    @Column(name = "new_balance", nullable = false)
    private BigDecimal newBalance;

    @Column(nullable = false)
    private BigDecimal difference;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    private User modifiedBy;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        if (modifiedAt == null) {
            modifiedAt = LocalDateTime.now();
        }
    }
}
