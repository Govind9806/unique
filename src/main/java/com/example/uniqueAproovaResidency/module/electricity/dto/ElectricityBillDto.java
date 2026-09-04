package com.example.uniqueAproovaResidency.module.electricity.dto;

import com.example.uniqueAproovaResidency.module.electricity.entity.ElectricityBill;
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
public class ElectricityBillDto {
    private String id;
    private String billingMonth;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status;
    private String documentId;
    private String createdByName;

    public static ElectricityBillDto fromEntity(ElectricityBill bill) {
        return ElectricityBillDto.builder()
                .id(bill.getId())
                .billingMonth(bill.getBillingMonth())
                .amount(bill.getAmount())
                .dueDate(bill.getDueDate())
                .paidDate(bill.getPaidDate())
                .status(bill.getStatus())
                .documentId(bill.getDocumentId())
                .createdByName(bill.getCreatedBy() != null ? bill.getCreatedBy().getName() : null)
                .build();
    }
}
