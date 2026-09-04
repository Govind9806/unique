package com.example.uniqueAproovaResidency.module.dashboard.service;

import com.example.uniqueAproovaResidency.module.dashboard.dto.AdminDashboardDto;
import com.example.uniqueAproovaResidency.module.dashboard.dto.ResidentDashboardDto;
import com.example.uniqueAproovaResidency.module.expense.service.ExpenseService;
import com.example.uniqueAproovaResidency.module.finance.service.FinanceService;
import com.example.uniqueAproovaResidency.module.flat.service.FlatService;
import com.example.uniqueAproovaResidency.module.maintenance.dto.BillDto;
import com.example.uniqueAproovaResidency.module.maintenance.service.BillService;
import com.example.uniqueAproovaResidency.module.meeting.service.MeetingService;
import com.example.uniqueAproovaResidency.module.notice.service.NoticeService;
import com.example.uniqueAproovaResidency.module.notification.service.NotificationService;
import com.example.uniqueAproovaResidency.module.responsibility.service.ResponsibilityService;
import com.example.uniqueAproovaResidency.module.user.dto.UserDto;
import com.example.uniqueAproovaResidency.module.user.service.UserService;
import com.example.uniqueAproovaResidency.module.work.service.WorkService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final FlatService flatService;
    private final BillService billService;
    private final FinanceService financeService;
    private final WorkService workService;
    private final NoticeService noticeService;
    private final MeetingService meetingService;
    private final NotificationService notificationService;
    private final ExpenseService expenseService;
    private final ResponsibilityService responsibilityService;

    @Transactional(readOnly = true)
    public ResidentDashboardDto getResidentDashboard(UserPrincipal currentUser) {
        UserDto userDto = userService.getUserById(currentUser.getId());

        List<BillDto> pendingBills = currentUser.getFlatId() != null
                ? billService.getPendingBillsForFlat(currentUser.getFlatId())
                : List.of();

        BigDecimal totalPending = pendingBills.stream()
                .map(BillDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate now = LocalDate.now();

        return ResidentDashboardDto.builder()
                .user(userDto)
                .flat(currentUser.getFlatId() != null ? flatService.getFlatById(currentUser.getFlatId()) : null)
                .pendingBills(pendingBills)
                .totalPendingAmount(totalPending)
                .financeSummary(financeService.getSummary())
                .recentWorks(workService.getAllWorks())
                .latestNotices(noticeService.getAllNotices())
                .upcomingMeetings(meetingService.getAllMeetings())
                .unreadNotificationCount(notificationService.getUnreadCount(currentUser.getId()))
                .apartmentPaymentStatusSummary(billService.getApartmentBillStatus(now.getMonthValue(), now.getYear()))
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard() {
        LocalDate now = LocalDate.now();

        return AdminDashboardDto.builder()
                .flats(flatService.getAllFlats())
                .apartmentBillStatus(billService.getApartmentBillStatus(now.getMonthValue(), now.getYear()))
                .financeSummary(financeService.getSummary())
                .pendingApprovals(expenseService.getPendingApprovals())
                .ongoingWorks(workService.getAllWorks())
                .currentResponsibilities(responsibilityService.getCurrentResponsibilities())
                .build();
    }
}
