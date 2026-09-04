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
public class ApartmentBillStatusDto {
    private int totalFlats;
    private int paidFlats;
    private int pendingFlats;
    private int overdueFlats;
    private BigDecimal totalExpected;
    private BigDecimal totalCollected;
    private BigDecimal totalPending;
    private List<FlatBillItem> flatStatuses;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FlatBillItem {
        private String flatId;
        private String flatNumber;
        private String status;
        private BigDecimal fixedMaintenanceAmount;
        private BigDecimal waterBillAmount;
        private BigDecimal waterUnitsConsumed;
        private BigDecimal totalMonthlyBill;
        private BigDecimal amount;
        private String residentName;
        private BigDecimal currentMonthPendingAmount;
        private BigDecimal overallPendingAmount;
        private int totalUnpaidMonths;
    }
}
