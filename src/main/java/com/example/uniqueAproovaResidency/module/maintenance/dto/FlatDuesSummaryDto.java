package com.example.uniqueAproovaResidency.module.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatDuesSummaryDto {
    private String flatId;
    private String flatNumber;
    private String residentName;
    private String residentPhone;
    private String currentMonthStatus;
    private BigDecimal fixedMaintenanceAmount;
    private BigDecimal waterBillAmount;
    private BigDecimal waterUnitsConsumed;
    private BigDecimal totalMonthlyBill;
    private BigDecimal currentMonthPendingAmount;
    private BigDecimal previousPendingArrears;
    private BigDecimal overallPendingAmount;
    private int totalUnpaidMonths;
    private int pastUnpaidMonths;
    private List<BillDto> pendingBills;
}
