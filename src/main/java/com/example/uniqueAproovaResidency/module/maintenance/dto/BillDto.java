package com.example.uniqueAproovaResidency.module.maintenance.dto;

import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
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
public class BillDto {
    private String id;
    private String flatId;
    private String flatNumber;
    private Integer billingMonth;
    private Integer billingYear;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;

    public static BillDto fromEntity(MaintenanceBill bill) {
        return BillDto.builder()
                .id(bill.getId())
                .flatId(bill.getFlat().getId())
                .flatNumber(bill.getFlat().getFlatNumber())
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .amount(bill.getAmount())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .build();
    }
}
