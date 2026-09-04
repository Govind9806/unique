package com.example.uniqueAproovaResidency.module.work.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.apartment.entity.Apartment;
import com.example.uniqueAproovaResidency.module.apartment.repository.ApartmentRepository;
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
import com.example.uniqueAproovaResidency.module.work.dto.CreateWorkRequest;
import com.example.uniqueAproovaResidency.module.work.dto.WorkDto;
import com.example.uniqueAproovaResidency.module.work.dto.WorkTimelineDto;
import com.example.uniqueAproovaResidency.module.work.entity.Work;
import com.example.uniqueAproovaResidency.module.work.entity.WorkTimeline;
import com.example.uniqueAproovaResidency.module.work.repository.WorkRepository;
import com.example.uniqueAproovaResidency.module.work.repository.WorkTimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.uniqueAproovaResidency.module.work.entity.WorkVote;
import com.example.uniqueAproovaResidency.module.work.repository.WorkVoteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;
    private final WorkVoteRepository workVoteRepository;
    private final WorkTimelineRepository timelineRepository;
    private final FlatRepository flatRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final LedgerRepository ledgerRepository;
    private final ExpenseRepository expenseRepository;
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
    public List<WorkDto> getAllWorks() {
        log.info("SERVICE [WorkService.getAllWorks]");
        List<WorkDto> works = workRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(w -> WorkDto.fromEntity(w, workVoteRepository.findByWorkId(w.getId())))
                .collect(Collectors.toList());
        log.info("SERVICE [WorkService.getAllWorks] -> Total repair works found: {}", works.size());
        return works;
    }

    @Transactional(readOnly = true)
    public WorkDto getWorkById(String id) {
        log.info("SERVICE [WorkService.getWorkById] -> Target Work ID: {}", id);
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work", "id", id));
        return WorkDto.fromEntity(work, workVoteRepository.findByWorkId(work.getId()));
    }

    @Transactional
    public WorkDto createWork(CreateWorkRequest request, User creator) {
        log.info("SERVICE [WorkService.createWork] -> Title: {}, Category: {}, Estimated Cost: ₹{}",
                request.getTitle(), request.getCategory(), request.getEstimatedCost());

        Flat flat = null;
        if (StringUtils.hasText(request.getResponsibleFlatId())) {
            flat = flatRepository.findById(request.getResponsibleFlatId()).orElse(null);
        }

        List<String> photos = request.getPhotoUrls() != null ? request.getPhotoUrls() : new java.util.ArrayList<>();
        String primaryPhoto = !photos.isEmpty() ? photos.get(0) : null;

        Work work = Work.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory().toUpperCase())
                .location(request.getLocation())
                .responsibleFlat(flat)
                .vendorName(request.getVendorName())
                .estimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : BigDecimal.ZERO)
                .actualCost(BigDecimal.ZERO)
                .status("PENDING_APPROVAL")
                .photoUrl(primaryPhoto)
                .photoUrls(photos)
                .createdBy(creator)
                .build();

        Work saved = workRepository.save(work);
        addTimelineEntry(saved, "PENDING_APPROVAL", "Repair request created with " + photos.size() + " photo(s) & sent to all 16 flats for voting", creator);
        log.info("SERVICE [WorkService.createWork] -> Repair work created with ID: {}", saved.getId());
        return WorkDto.fromEntity(saved);
    }

    @Transactional
    public WorkDto approveWork(String workId, User approver) {
        log.info("SERVICE [WorkService.approveWork] -> Target Work ID: {}, Approver: {}", workId, approver != null ? approver.getName() : "System");
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Work", "id", workId));

        work.setStatus("APPROVED");
        work.setApprovedBy(approver);
        Work saved = workRepository.save(work);

        addTimelineEntry(saved, "APPROVED", "Work approved by Treasurer/Admin", approver);
        log.info("SERVICE [WorkService.approveWork] -> Work ID {} approved", saved.getId());
        return WorkDto.fromEntity(saved);
    }

    @Transactional
    public WorkDto startWork(String workId, User user) {
        log.info("SERVICE [WorkService.startWork] -> Target Work ID: {}", workId);
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Work", "id", workId));

        work.setStatus("IN_PROGRESS");
        work.setStartDate(LocalDate.now());
        Work saved = workRepository.save(work);

        addTimelineEntry(saved, "IN_PROGRESS", "Work started on site", user);
        log.info("SERVICE [WorkService.startWork] -> Work ID {} marked IN_PROGRESS", saved.getId());
        return WorkDto.fromEntity(saved);
    }

    @Transactional
    public WorkDto completeWork(String workId, BigDecimal actualCost, User user) {
        log.info("SERVICE [WorkService.completeWork] -> Target Work ID: {}, Actual Cost: ₹{}", workId, actualCost);
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Work", "id", workId));

        work.setStatus("COMPLETED");
        if (actualCost != null) {
            work.setActualCost(actualCost);
        }
        work.setCompletionDate(LocalDate.now());
        Work saved = workRepository.save(work);

        addTimelineEntry(saved, "COMPLETED", "Work completed successfully", user);
        log.info("SERVICE [WorkService.completeWork] -> Work ID {} marked COMPLETED", saved.getId());
        return WorkDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkTimelineDto> getWorkTimeline(String workId) {
        log.info("SERVICE [WorkService.getWorkTimeline] -> Work ID: {}", workId);
        return timelineRepository.findByWorkIdOrderByCreatedAtAsc(workId).stream()
                .map(WorkTimelineDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkDto voteOnWork(String workId, String voteChoice, String reason, User voter) {
        log.info("SERVICE [WorkService.voteOnWork] -> Work ID: {}, Vote: {}, Voter: {}", workId, voteChoice, voter != null ? voter.getName() : "Unknown");

        if (voter == null || voter.getRole() == null || voter.getRole().name().contains("ADMIN")) {
            throw new AccessDeniedException("Admin accounts have read-only monitoring access and cannot cast votes on repair proposals.");
        }

        if (voter.getFlat() == null) {
            throw new BusinessRuleException("NO_FLAT_ASSIGNED", "User must belong to a resident flat to vote on repair proposals.");
        }

        String voterFlat = voter.getFlat().getFlatNumber();

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Work", "id", workId));

        if (workVoteRepository.existsByWorkIdAndFlatNumber(workId, voterFlat)) {
            throw new BusinessRuleException("DUPLICATE_VOTE", "Flat " + voterFlat + " has already cast a vote for this repair proposal. Votes are locked and cannot be duplicated or altered.");
        }

        String voteType = "REJECT".equalsIgnoreCase(voteChoice) ? "REJECT" : "APPROVE";
        WorkVote voteObj = WorkVote.builder()
                .id(UUID.randomUUID().toString())
                .work(work)
                .flatNumber(voterFlat)
                .vote(voteType)
                .reason(StringUtils.hasText(reason) ? reason.trim() : null)
                .votedBy(voter)
                .votedAt(LocalDateTime.now())
                .build();
        workVoteRepository.save(voteObj);

        List<WorkVote> votes = workVoteRepository.findByWorkId(work.getId());
        int accepts = (int) votes.stream().filter(v -> "APPROVE".equalsIgnoreCase(v.getVote())).count();
        int rejects = (int) votes.stream().filter(v -> "REJECT".equalsIgnoreCase(v.getVote())).count();

        // 16 Flats total -> Majority is 9 votes
        if (accepts >= 9) {
            work.setStatus("APPROVED");

            Notice notice = Notice.builder()
                    .id(UUID.randomUUID().toString())
                    .title("✅ REPAIR REQUEST APPROVED BY SOCIETY MAJORITY (" + accepts + "/16 VOTES)")
                    .description("Repair request '" + work.getTitle() + "' reached majority approval (" + accepts + " Approve vs " + rejects + " Reject). Sent to Maintenance Flat for action.")
                    .priority("URGENT")
                    .isPinned(true)
                    .expiryDate(LocalDate.now().plusDays(30))
                    .createdBy(voter)
                    .build();
            noticeRepository.save(notice);

            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("REPAIR_VOTE_APPROVED")
                        .title("✅ Repair Request Approved (" + accepts + " vs " + rejects + " Votes)")
                        .message("Repair request '" + work.getTitle() + "' was approved by society majority (9+ votes).")
                        .referenceType("WORK")
                        .referenceId(work.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }
        } else if (rejects >= 9) {
            work.setStatus("REJECTED");

            Notice notice = Notice.builder()
                    .id(UUID.randomUUID().toString())
                    .title("❌ REPAIR REQUEST REJECTED BY SOCIETY MAJORITY (" + rejects + "/16 VOTES)")
                    .description("Repair request '" + work.getTitle() + "' was REJECTED by majority (" + rejects + " Reject vs " + accepts + " Approve). Request closed.")
                    .priority("IMPORTANT")
                    .isPinned(false)
                    .expiryDate(LocalDate.now().plusDays(30))
                    .createdBy(voter)
                    .build();
            noticeRepository.save(notice);

            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                Notification notif = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(u)
                        .type("REPAIR_VOTE_REJECTED")
                        .title("❌ Repair Request Rejected (" + rejects + " vs " + accepts + " Votes)")
                        .message("Repair request '" + work.getTitle() + "' was rejected by society majority.")
                        .referenceType("WORK")
                        .referenceId(work.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notif);
            }
        } else {
            work.setStatus("PENDING_APPROVAL");
        }

        Work saved = workRepository.save(work);
        addTimelineEntry(saved, saved.getStatus(), "Vote cast by Flat " + voterFlat + ": " + voteType + (StringUtils.hasText(reason) ? " (" + reason.trim() + ")" : ""), voter);
        return WorkDto.fromEntity(saved, workVoteRepository.findByWorkId(saved.getId()));
    }

    @Transactional
    public WorkDto maintenanceRespond(String workId, String decision, String message, User maintenanceUser) {
        log.info("SERVICE [WorkService.maintenanceRespond] -> Work ID: {}, Decision: {}, User: {}", workId, decision, maintenanceUser != null ? maintenanceUser.getName() : "Maintenance");

        if (maintenanceUser == null || maintenanceUser.getRole() == null || maintenanceUser.getRole().name().contains("ADMIN")) {
            throw new AccessDeniedException("Admin accounts have read-only monitoring access and cannot submit Maintenance Flat action responses.");
        }

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Work", "id", workId));

        String dec = "ACCEPT".equalsIgnoreCase(decision) || "ACCEPTED".equalsIgnoreCase(decision) ? "ACCEPTED" : "REJECTED";
        work.setMaintenanceDecision(dec);
        work.setMaintenanceMessage(message != null ? message.trim() : "");
        work.setMaintenanceRespondedAt(LocalDateTime.now());

        if ("ACCEPTED".equalsIgnoreCase(dec)) {
            work.setStatus("ACCEPTED_BY_MAINTENANCE");
        } else {
            work.setStatus("REJECTED_BY_MAINTENANCE");
        }

        Work saved = workRepository.save(work);
        addTimelineEntry(saved, saved.getStatus(), "Maintenance Flat responded: " + dec + (StringUtils.hasText(message) ? " (" + message.trim() + ")" : ""), maintenanceUser);
        return WorkDto.fromEntity(saved, workVoteRepository.findByWorkId(saved.getId()));
    }

    @Transactional
    public WorkDto startAndPayWork(String workId, BigDecimal finalCost, String photoUrl, User user) {
        log.info("SERVICE [WorkService.startAndPayWork] -> Work ID: {}, Cost: ₹{}, Photo: {}", workId, finalCost, photoUrl);
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Work", "id", workId));

        if (!StringUtils.hasText(photoUrl)) {
            throw new BusinessRuleException("PHOTO_REQUIRED", "Proof photo of completed work/receipt is MANDATORY when starting & paying for repair work.");
        }

        if (finalCost == null || finalCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("INVALID_AMOUNT", "Final price must be greater than zero.");
        }

        // Negative Balance Guard check
        BigDecimal currentBalance = getLiveCurrentBalance();
        if (finalCost.compareTo(currentBalance) > 0) {
            BigDecimal deficit = finalCost.subtract(currentBalance);
            throw new BusinessRuleException("INSUFFICIENT_FUNDS",
                    "Cannot start & pay for repair! Cost ₹" + finalCost + " exceeds current society balance ₹" + currentBalance + ". Deficit: ₹" + deficit + ".");
        }

        work.setStatus("COMPLETED");
        work.setActualCost(finalCost);
        work.setPhotoUrl(photoUrl);
        work.setStartDate(LocalDate.now());
        work.setCompletionDate(LocalDate.now());

        Work saved = workRepository.save(work);

        // Record Expense & Ledger Income Deduction
        Expense expense = Expense.builder()
                .id(UUID.randomUUID().toString())
                .category(saved.getCategory())
                .description("Paid Repair Work: " + saved.getTitle() + " (Vendor: " + (saved.getVendorName() != null ? saved.getVendorName() : "Contractor") + ")")
                .amount(finalCost)
                .expenseDate(LocalDate.now())
                .vendor(saved.getVendorName())
                .relatedWork(saved)
                .createdBy(user)
                .approvalStatus("APPROVED")
                .paymentStatus("PAID")
                .approvedBy(user)
                .approvedAt(LocalDateTime.now())
                .build();
        expenseRepository.save(expense);

        LedgerTransaction ledgerTx = LedgerTransaction.builder()
                .id(UUID.randomUUID().toString())
                .type("EXPENSE")
                .category(saved.getCategory())
                .amount(finalCost)
                .description("Paid Repair Work: " + saved.getTitle() + " [Proof Uploaded]")
                .referenceType("WORK")
                .referenceId(saved.getId())
                .transactionDate(LocalDateTime.now())
                .createdBy(user)
                .build();
        ledgerRepository.save(ledgerTx);

        addTimelineEntry(saved, "COMPLETED", "Work started & paid ₹" + finalCost + " with uploaded proof photo", user);

        // Broadcast Completion Notice with Photo
        Notice notice = Notice.builder()
                .id(UUID.randomUUID().toString())
                .title("🔧 REPAIR WORK COMPLETED & PAID: ₹" + finalCost)
                .description("Repair work '" + saved.getTitle() + "' was completed & paid ₹" + finalCost + ". Proof photo uploaded by Maintenance.")
                .priority("IMPORTANT")
                .isPinned(false)
                .expiryDate(LocalDate.now().plusDays(30))
                .createdBy(user)
                .build();
        noticeRepository.save(notice);

        return WorkDto.fromEntity(saved);
    }

    private void addTimelineEntry(Work work, String status, String description, User user) {
        log.info("SERVICE [WorkService.addTimelineEntry] -> Work ID: {}, Status: {}, Desc: {}", work.getId(), status, description);
        WorkTimeline timeline = WorkTimeline.builder()
                .id(UUID.randomUUID().toString())
                .work(work)
                .status(status)
                .description(description)
                .createdBy(user)
                .build();
        timelineRepository.save(timeline);
    }
}
