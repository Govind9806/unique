package com.example.uniqueAproovaResidency.module.dashboard.dto;

import com.example.uniqueAproovaResidency.module.finance.dto.FinanceSummaryDto;
import com.example.uniqueAproovaResidency.module.flat.dto.FlatDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.ApartmentBillStatusDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.BillDto;
import com.example.uniqueAproovaResidency.module.meeting.dto.MeetingDto;
import com.example.uniqueAproovaResidency.module.notice.dto.NoticeDto;
import com.example.uniqueAproovaResidency.module.user.dto.UserDto;
import com.example.uniqueAproovaResidency.module.work.dto.WorkDto;
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
public class ResidentDashboardDto {
    private UserDto user;
    private FlatDto flat;
    private List<BillDto> pendingBills;
    private BigDecimal totalPendingAmount;
    private FinanceSummaryDto financeSummary;
    private List<WorkDto> recentWorks;
    private List<NoticeDto> latestNotices;
    private List<MeetingDto> upcomingMeetings;
    private long unreadNotificationCount;
    private ApartmentBillStatusDto apartmentPaymentStatusSummary;
}
