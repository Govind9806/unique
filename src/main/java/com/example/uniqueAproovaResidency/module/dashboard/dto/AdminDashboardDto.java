package com.example.uniqueAproovaResidency.module.dashboard.dto;

import com.example.uniqueAproovaResidency.module.expense.dto.ExpenseDto;
import com.example.uniqueAproovaResidency.module.finance.dto.FinanceSummaryDto;
import com.example.uniqueAproovaResidency.module.flat.dto.FlatDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.ApartmentBillStatusDto;
import com.example.uniqueAproovaResidency.module.responsibility.dto.ResponsibilityDto;
import com.example.uniqueAproovaResidency.module.work.dto.WorkDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private List<FlatDto> flats;
    private ApartmentBillStatusDto apartmentBillStatus;
    private FinanceSummaryDto financeSummary;
    private List<ExpenseDto> pendingApprovals;
    private List<WorkDto> ongoingWorks;
    private List<ResponsibilityDto> currentResponsibilities;
}
