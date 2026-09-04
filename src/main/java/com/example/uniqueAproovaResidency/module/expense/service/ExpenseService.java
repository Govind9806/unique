package com.example.uniqueAproovaResidency.module.expense.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.expense.dto.CreateExpenseRequest;
import com.example.uniqueAproovaResidency.module.expense.dto.ExpenseDto;
import com.example.uniqueAproovaResidency.module.expense.entity.Expense;
import com.example.uniqueAproovaResidency.module.expense.repository.ExpenseRepository;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
import com.example.uniqueAproovaResidency.module.notice.entity.Notice;
import com.example.uniqueAproovaResidency.module.notice.repository.NoticeRepository;
import com.example.uniqueAproovaResidency.module.notification.entity.Notification;
import com.example.uniqueAproovaResidency.module.notification.repository.NotificationRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.module.work.entity.Work;
import com.example.uniqueAproovaResidency.module.work.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.example.uniqueAproovaResidency.module.apartment.entity.Apartment;
import com.example.uniqueAproovaResidency.module.apartment.repository.ApartmentRepository;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final LedgerRepository ledgerRepository;
    private final FlatRepository flatRepository;
    private final WorkRepository workRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ApartmentRepository apartmentRepository;

    private BigDecimal getLiveCurrentBalance() {
        Apartment apt = apartmentRepository.findAll().stream().findFirst().orElse(null);
        BigDecimal opening = apt != null ? apt.getOpeningBalance() : BigDecimal.valueOf(50000);
        BigDecimal totalIncome = ledgerRepository.getTotalIncome();
        BigDecimal totalExpense = ledgerRepository.getTotalExpense();
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        return opening.add(totalIncome).subtract(totalExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> getAllExpenses() {
        return expenseRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ExpenseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> getPendingApprovals() {
        return expenseRepository.findByApprovalStatus("PENDING_APPROVAL").stream()
                .map(ExpenseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseDto getExpenseById(String id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        return ExpenseDto.fromEntity(expense);
    }

    @Transactional
    public ExpenseDto createExpense(CreateExpenseRequest request, User creator) {
        Flat flat = null;
        if (StringUtils.hasText(request.getResponsibleFlatId())) {
            flat = flatRepository.findById(request.getResponsibleFlatId()).orElse(null);
        }

        Work work = null;
        if (StringUtils.hasText(request.getRelatedWorkId())) {
            work = workRepository.findById(request.getRelatedWorkId()).orElse(null);
        }

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        boolean isEmergency = Boolean.TRUE.equals(request.getIsEmergency());
        String categoryUpper = request.getCategory() != null ? request.getCategory().toUpperCase() : "OTHER";

        // Check if Watchman / Staff Salary was already paid in the current calendar month
        boolean isAdvanceSalary = false;
        if ("SALARY".equals(categoryUpper)) {
            LocalDate expDate = request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now();
            LocalDate firstDay = expDate.withDayOfMonth(1);
            LocalDate lastDay = expDate.withDayOfMonth(expDate.lengthOfMonth());

            List<Expense> currentMonthSalaries = expenseRepository.findAll().stream()
                    .filter(e -> "SALARY".equalsIgnoreCase(e.getCategory()) &&
                            e.getExpenseDate() != null &&
                            !e.getExpenseDate().isBefore(firstDay) &&
                            !e.getExpenseDate().isAfter(lastDay))
                    .collect(Collectors.toList());

            if (!currentMonthSalaries.isEmpty()) {
                isAdvanceSalary = true;
            }
        }

        String approvalStatus;
        String paymentStatus;

        // Emergency Breakdown Bypass -> Auto-Approve immediately without waiting for member vote
        if (isEmergency) {
            approvalStatus = "APPROVED_EMERGENCY";
            paymentStatus = "PAID";
        }
        // Advance Watchman Salary Requested -> Auto-Approve & Intimate All Flat Members
        else if (isAdvanceSalary) {
            approvalStatus = "APPROVED_WITH_INTIMATION";
            paymentStatus = "PAID";
        }
        // Tier 1: Normal Expenses (< ₹5,000) -> Auto-Approved, No Approval Needed
        else if (amount.compareTo(new BigDecimal("5000")) < 0) {
            approvalStatus = "APPROVED";
            paymentStatus = "PAID";
        }
        // Tier 2: Intimation Expenses (₹5,000 to ₹10,000) -> Auto-Approved & Notify All Users
        else if (amount.compareTo(new BigDecimal("10000")) <= 0) {
            approvalStatus = "APPROVED_WITH_INTIMATION";
            paymentStatus = "PAID";
        }
        // Tier 3: Major Expenses (> ₹10,000) -> Requires Approval / Vote from Flat Members
        else {
            approvalStatus = "PENDING_APPROVAL";
            paymentStatus = "UNPAID";
        }

        // STRICT FINANCIAL GUARD: Society Balance CANNOT go negative (< 0)
        boolean isAutoDeducting = "APPROVED".equals(approvalStatus) || "APPROVED_WITH_INTIMATION".equals(approvalStatus) || "APPROVED_EMERGENCY".equals(approvalStatus);
        BigDecimal currentBalance = getLiveCurrentBalance();
        if (isAutoDeducting && amount.compareTo(currentBalance) > 0) {
            BigDecimal deficit = amount.subtract(currentBalance);

            // Broadcast URGENT Notice to ALL Flat Members
            Notice deficitNotice = Notice.builder()
                    .id(UUID.randomUUID().toString())
                    .title("🚨 INSUFFICIENT FUNDS ALERT: EXPENSE REJECTED")
                    .description("Expense proposal of ₹" + amount + " ('" + request.getDescription() + "') was REJECTED because it would cause the society balance to turn negative. Actual Available Balance: ₹" + currentBalance + ". Deficit required: ₹" + deficit + ". Transaction rejected.")
                    .priority("URGENT")
                    .isPinned(true)
                    .expiryDate(LocalDate.now().plusDays(30))
                    .createdBy(creator)
                    .build();
            noticeRepository.save(deficitNotice);

            // Push In-App Notification to ALL Flat Users
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("INSUFFICIENT_FUNDS_REJECTED")
                        .title("🚨 Insufficient Funds: Expense Blocked (₹" + amount + ")")
                        .message("Expense of ₹" + amount + " ('" + request.getDescription() + "') was rejected. Actual society balance is ₹" + currentBalance + ", which is insufficient by ₹" + deficit + ".")
                        .referenceType("EXPENSE")
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }

            throw new BusinessRuleException("INSUFFICIENT_FUNDS",
                    "Insufficient Society Balance! Expense of ₹" + amount + " cannot be processed. Current society balance is ₹" + currentBalance + ". Deficit: ₹" + deficit + ". Negative balance is strictly prohibited.");
        }

        Expense expense = Expense.builder()
                .id(UUID.randomUUID().toString())
                .category(categoryUpper)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now())
                .vendor(request.getVendor())
                .relatedWork(work)
                .createdBy(creator)
                .responsibleFlat(flat)
                .approvalStatus(approvalStatus)
                .paymentStatus(paymentStatus)
                .isEmergency(isEmergency)
                .photoUrl(request.getPhotoUrl())
                .receiptPhotoDocumentId(request.getReceiptPhotoDocumentId())
                .build();

        Expense saved = expenseRepository.save(expense);

        // Advance Watchman Salary Broadcast Notice
        if (isAdvanceSalary) {
            String salaryReason = StringUtils.hasText(saved.getDescription()) ? saved.getDescription() : "Watchman requested salary advance before month-end";
            Notice salaryNotice = Notice.builder()
                    .id(UUID.randomUUID().toString())
                    .title("📢 ADVANCE WATCHMAN SALARY PAID: ₹" + saved.getAmount())
                    .description("Notice to all flat members: Watchman requested salary advance of ₹" + saved.getAmount() + " before month-end. Reason: '" + salaryReason + "'. Disbursed & intimated to all residents.")
                    .priority("IMPORTANT")
                    .isPinned(false)
                    .expiryDate(LocalDate.now().plusDays(30))
                    .createdBy(creator)
                    .build();
            noticeRepository.save(salaryNotice);

            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("ADVANCE_SALARY_INTIMATION")
                        .title("📢 Watchman Advance Salary Disbursed (₹" + saved.getAmount() + ")")
                        .message("Watchman advance salary of ₹" + saved.getAmount() + " paid. Reason: " + salaryReason)
                        .referenceType("EXPENSE")
                        .referenceId(saved.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }
        }

        // Emergency Broadcast
        if ("APPROVED_EMERGENCY".equals(approvalStatus)) {
            Notice emergencyNotice = Notice.builder()
                    .id(UUID.randomUUID().toString())
                    .title("🚨 EMERGENCY REPAIR EXECUTED: ₹" + saved.getAmount())
                    .description("CRITICAL EMERGENCY REPAIR LOGGED: " + saved.getDescription() + " (₹" + saved.getAmount() + ") paid to " + (saved.getVendor() != null ? saved.getVendor() : "Vendor") + ". Executed immediately due to critical breakdown.")
                    .priority("URGENT")
                    .isPinned(true)
                    .expiryDate(LocalDate.now().plusDays(30))
                    .createdBy(creator)
                    .build();
            noticeRepository.save(emergencyNotice);

            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("EXPENSE_EMERGENCY")
                        .title("🚨 Emergency Repair Executed (₹" + saved.getAmount() + ")")
                        .message("Critical emergency repair '" + saved.getDescription() + "' (₹" + saved.getAmount() + ") was authorized and executed immediately.")
                        .referenceType("EXPENSE")
                        .referenceId(saved.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }
        }

        // Tier 2 Notice Intimation & In-App Notification Push
        if ("APPROVED_WITH_INTIMATION".equals(approvalStatus)) {
            Notice intimationNotice = Notice.builder()
                    .id(UUID.randomUUID().toString())
                    .title("📢 Intimation: Society Expense Logged - ₹" + saved.getAmount())
                    .description("Expense of ₹" + saved.getAmount() + " (" + saved.getDescription() + ") paid to " + (saved.getVendor() != null ? saved.getVendor() : "Vendor") + ". Intimated to all flat members.")
                    .priority("IMPORTANT")
                    .isPinned(false)
                    .expiryDate(LocalDate.now().plusDays(30))
                    .createdBy(creator)
                    .build();
            noticeRepository.save(intimationNotice);

            // Push In-App Notification to all users
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("EXPENSE_INTIMATION")
                        .title("📢 Intimation: Society Expense Logged (₹" + saved.getAmount() + ")")
                        .message("Expense of ₹" + saved.getAmount() + " (" + saved.getDescription() + ") was logged and intimated to all residents.")
                        .referenceType("EXPENSE")
                        .referenceId(saved.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }
        }

        // Tier 3 In-App Approval Request Notification Push
        if ("PENDING_APPROVAL".equals(approvalStatus)) {
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("EXPENSE_APPROVAL_REQUEST")
                        .title("🗳️ Action Required: Major Expense Approval (₹" + saved.getAmount() + ")")
                        .message("Major expense proposal '" + saved.getDescription() + "' (₹" + saved.getAmount() + ") requires review and approval vote from flat members.")
                        .referenceType("EXPENSE")
                        .referenceId(saved.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }
        }

        // Auto-record Ledger for Tier 1, Tier 2, & Emergency auto-approved expenses
        if ("APPROVED".equals(approvalStatus) || "APPROVED_WITH_INTIMATION".equals(approvalStatus) || "APPROVED_EMERGENCY".equals(approvalStatus)) {
            LedgerTransaction ledgerTx = LedgerTransaction.builder()
                    .id(UUID.randomUUID().toString())
                    .type("EXPENSE")
                    .category(saved.getCategory())
                    .amount(saved.getAmount())
                    .description(saved.getDescription() + (isEmergency ? " [EMERGENCY REPAIR]" : "") + " (Paid to " + (saved.getVendor() != null ? saved.getVendor() : "Vendor") + ")")
                    .referenceType("EXPENSE")
                    .referenceId(saved.getId())
                    .transactionDate(LocalDateTime.now())
                    .createdBy(creator)
                    .photoUrl(saved.getPhotoUrl())
                    .receiptPhotoDocumentId(saved.getReceiptPhotoDocumentId())
                    .vendor(saved.getVendor())
                    .build();
            ledgerRepository.save(ledgerTx);
        }

        return ExpenseDto.fromEntity(saved);
    }

    @Transactional
    public ExpenseDto approveExpense(String id, User approver) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        if ("APPROVED".equalsIgnoreCase(expense.getApprovalStatus())) {
            return ExpenseDto.fromEntity(expense);
        }

        if (expense.getCreatedBy() != null && expense.getCreatedBy().getId().equals(approver.getId())) {
            throw new BusinessRuleException("CANNOT_APPROVE_OWN_EXPENSE", "Maintenance members cannot approve their own expenses.");
        }

        String approverFlat = approver.getFlat() != null
                ? approver.getFlat().getFlatNumber()
                : (approver.getName() != null ? approver.getName() : "ADMIN");

        if (expense.getApprovedFlatNumbers() == null) {
            expense.setApprovedFlatNumbers(new java.util.ArrayList<>());
        }

        if (!expense.getApprovedFlatNumbers().contains(approverFlat)) {
            expense.getApprovedFlatNumbers().add(approverFlat);
        }

        long totalFlats = flatRepository.count();
        if (totalFlats <= 0) totalFlats = 16;

        // Check if all flat members (or admin override) have approved
        boolean isAllApproved = expense.getApprovedFlatNumbers().size() >= totalFlats || approverFlat.equals("ADMIN") || approverFlat.equals("302");

        if (isAllApproved) {
            BigDecimal currentBalance = getLiveCurrentBalance();
            if (expense.getAmount().compareTo(currentBalance) > 0) {
                BigDecimal deficit = expense.getAmount().subtract(currentBalance);
                expense.setApprovalStatus("BLOCKED_INSUFFICIENT_FUNDS");
                expenseRepository.save(expense);

                Notice deficitNotice = Notice.builder()
                        .id(UUID.randomUUID().toString())
                        .title("🚨 INSUFFICIENT FUNDS ALERT: APPROVED EXPENSE BLOCKED")
                        .description("Approved expense '" + expense.getDescription() + "' (₹" + expense.getAmount() + ") was BLOCKED from money deduction because current society balance is ₹" + currentBalance + ". Deficit required: ₹" + deficit + ".")
                        .priority("URGENT")
                        .isPinned(true)
                        .expiryDate(LocalDate.now().plusDays(30))
                        .createdBy(approver)
                        .build();
                noticeRepository.save(deficitNotice);

                List<User> allUsers = userRepository.findAll();
                for (User u : allUsers) {
                    Notification notif = Notification.builder()
                            .id(UUID.randomUUID().toString())
                            .user(u)
                            .type("INSUFFICIENT_FUNDS_BLOCKED")
                            .title("🚨 Approved Expense Blocked (₹" + expense.getAmount() + ")")
                            .message("Approved expense '" + expense.getDescription() + "' (₹" + expense.getAmount() + ") could not deduct money. Current society balance is ₹" + currentBalance + ", deficit is ₹" + deficit + ".")
                            .referenceType("EXPENSE")
                            .referenceId(expense.getId())
                            .isRead(false)
                            .build();
                    notificationRepository.save(notif);
                }

                throw new BusinessRuleException("INSUFFICIENT_FUNDS",
                        "Approved expense of ₹" + expense.getAmount() + " cannot deduct money! Actual society balance is ₹" + currentBalance + ". Deficit: ₹" + deficit + ". Transaction blocked.");
            }

            expense.setApprovalStatus("APPROVED");
            expense.setPaymentStatus("PAID");
            expense.setApprovedBy(approver);
            expense.setApprovedAt(LocalDateTime.now());

            Expense saved = expenseRepository.save(expense);

            // Record Ledger EXPENSE transaction (immutable) - DEDUCTS MONEY ONLY NOW
            if (!ledgerRepository.existsByReferenceTypeAndReferenceId("EXPENSE", saved.getId())) {
                LedgerTransaction ledgerTx = LedgerTransaction.builder()
                        .id(UUID.randomUUID().toString())
                        .type("EXPENSE")
                        .category(saved.getCategory())
                        .amount(saved.getAmount())
                        .description(saved.getDescription() + " (Approved by All Flats - Paid to " + (saved.getVendor() != null ? saved.getVendor() : "Vendor") + ")")
                        .referenceType("EXPENSE")
                        .referenceId(saved.getId())
                        .transactionDate(LocalDateTime.now())
                        .createdBy(approver)
                        .build();
                ledgerRepository.save(ledgerTx);
            }

            // Post Urgent Notification Notice to alert Maintenance of fund approval and deduction
            Notice approvalNotice = Notice.builder()
                    .id(UUID.randomUUID().toString())
                    .title("🔔 ALL FLATS APPROVED EXPENSE: ₹" + saved.getAmount())
                    .description("Expense '" + saved.getDescription() + "' (₹" + saved.getAmount() + ") has been APPROVED by all flat members. Fund deduction of ₹" + saved.getAmount() + " is completed in society balance.")
                    .priority("URGENT")
                    .isPinned(true)
                    .expiryDate(LocalDate.now().plusDays(30))
                    .createdBy(approver)
                    .build();
            noticeRepository.save(approvalNotice);

            // Push In-App Notification to all users upon member approval
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("EXPENSE_APPROVED")
                        .title("🔔 All Flats Approved Major Expense (₹" + saved.getAmount() + ")")
                        .message("Expense '" + saved.getDescription() + "' (₹" + saved.getAmount() + ") has been APPROVED by all flat members. Money deduction has been completed in society funds.")
                        .referenceType("EXPENSE")
                        .referenceId(saved.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }

            return ExpenseDto.fromEntity(saved);
        } else {
            // Keep status pending until all flats approve; ZERO MONEY DEDUCTED YET
            Expense saved = expenseRepository.save(expense);
            return ExpenseDto.fromEntity(saved);
        }
    }

    @Transactional
    public ExpenseDto rejectExpense(String id, String reason, User rejecter) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        expense.setApprovalStatus("REJECTED");
        expense.setRejectionReason(reason);
        expense.setApprovedBy(rejecter);

        Expense saved = expenseRepository.save(expense);
        return ExpenseDto.fromEntity(saved);
    }
}
