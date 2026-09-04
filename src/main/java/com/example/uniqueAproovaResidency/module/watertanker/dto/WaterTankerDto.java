package com.example.uniqueAproovaResidency.module.watertanker.dto;

import com.example.uniqueAproovaResidency.module.watertanker.entity.WaterTanker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterTankerDto {
    private String id;
    private String supplier;
    private LocalDate tankerDate;
    private Integer quantityLiters;
    private BigDecimal amount;
    private String reason;
    private String invoiceDocumentId;
    private String recordedByName;
    private String approvalStatus;
    private String paymentStatus;

    public static WaterTankerDto fromEntity(WaterTanker tanker) {
        return WaterTankerDto.builder()
                .id(tanker.getId())
                .supplier(tanker.getSupplier())
                .tankerDate(tanker.getTankerDate())
                .quantityLiters(tanker.getQuantityLiters())
                .amount(tanker.getAmount())
                .reason(tanker.getReason())
                .invoiceDocumentId(tanker.getInvoiceDocumentId())
                .recordedByName(tanker.getRecordedBy() != null ? tanker.getRecordedBy().getName() : null)
                .approvalStatus(tanker.getApprovalStatus())
                .paymentStatus(tanker.getPaymentStatus())
                .build();
    }
}
