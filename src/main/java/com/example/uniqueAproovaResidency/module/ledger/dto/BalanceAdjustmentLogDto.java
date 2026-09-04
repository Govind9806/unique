package com.example.uniqueAproovaResidency.module.ledger.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BalanceAdjustmentLogDto {
    private String id;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private BigDecimal difference;
    private String reason;
    private String modifiedByUserId;
    private String modifiedByName;
    private LocalDateTime modifiedAt;
}
