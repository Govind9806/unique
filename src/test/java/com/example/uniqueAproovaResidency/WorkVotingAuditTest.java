package com.example.uniqueAproovaResidency;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.module.expense.repository.ExpenseRepository;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.role.Role;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.module.work.dto.CreateWorkRequest;
import com.example.uniqueAproovaResidency.module.work.dto.WorkDto;
import com.example.uniqueAproovaResidency.module.work.repository.WorkVoteRepository;
import com.example.uniqueAproovaResidency.module.work.service.WorkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "SPRING_APPLICATION_NAME=uniqueAproovaResidency",
    "SERVER_PORT=8080",
    "SERVER_SERVLET_CONTEXT_PATH=/",
    "DATABASE_URL=jdbc:h2:mem:aproovadb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
    "DATABASE_USERNAME=sa",
    "DATABASE_PASSWORD=",
    "DATABASE_DRIVER=org.h2.Driver",
    "JPA_DDL_AUTO=update",
    "JPA_SHOW_SQL=true",
    "HIBERNATE_FORMAT_SQL=true",
    "FLYWAY_ENABLED=true",
    "FLYWAY_BASELINE_ON_MIGRATE=true",
    "FLYWAY_LOCATIONS=classpath:db/migration",
    "SPRINGDOC_API_DOCS_PATH=/v3/api-docs",
    "SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html",
    "JWT_SECRET=9a4f2c8d7b1e3f5a6b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a",
    "JWT_EXPIRATION_MS=86400000",
    "JWT_REFRESH_EXPIRATION_MS=604800000",
    "FILE_STORAGE_PATH=./uploads",
    "PAYMENT_PROVIDER=direct_upi",
    "UPI_MERCHANT_VPA=aproovaresidency@upi",
    "UPI_MERCHANT_NAME=Aproova Residency Society",
    "PAYMENT_WEBHOOK_SECRET=aproova_webhook_secret_key_2026"
})
class WorkVotingAuditTest {

    @Autowired
    private WorkService workService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlatRepository flatRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private WorkVoteRepository workVoteRepository;

    private User createAndSaveUser(String name, String email, Role role, Flat flat) {
        User u = User.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .email(email)
                .password("password123")
                .role(role)
                .flat(flat)
                .status("ACTIVE")
                .build();
        return userRepository.save(u);
    }

    private Flat getOrCreateFlat(String flatNumber) {
        return flatRepository.findByFlatNumber(flatNumber)
                .orElseGet(() -> flatRepository.save(Flat.builder()
                        .id("flat-" + flatNumber)
                        .flatNumber(flatNumber)
                        .floor(1)
                        .status("OCCUPIED")
                        .build()));
    }

    @Test
    @Transactional
    void testAdminCannotVote_ThrowsAccessDeniedException() {
        User admin = createAndSaveUser("Society Admin", "admin@aproova.com", Role.ADMIN, null);

        CreateWorkRequest request = new CreateWorkRequest();
        request.setTitle("Pipe Burst Emergency");
        request.setDescription("Water line burst");
        request.setEstimatedCost(BigDecimal.valueOf(5000));
        request.setCategory("REPAIR");

        WorkDto work = workService.createWork(request, admin);

        assertThrows(AccessDeniedException.class, () -> {
            workService.voteOnWork(work.getId(), "APPROVE", "Admin trying to vote", admin);
        });
    }

    @Test
    @Transactional
    void testAdminCannotRespondMaintenance_ThrowsAccessDeniedException() {
        User admin = createAndSaveUser("Society Admin 2", "admin2@aproova.com", Role.ADMIN, null);

        CreateWorkRequest request = new CreateWorkRequest();
        request.setTitle("Lift Repair");
        request.setEstimatedCost(BigDecimal.valueOf(2000));
        request.setCategory("LIFT");

        WorkDto work = workService.createWork(request, admin);

        assertThrows(AccessDeniedException.class, () -> {
            workService.maintenanceRespond(work.getId(), "ACCEPTED", "Admin attempting maintenance response", admin);
        });
    }

    @Test
    @Transactional
    void testSameFlatCannotVoteTwice_ThrowsDuplicateVoteException() {
        Flat flat101 = getOrCreateFlat("101");

        User residentA = createAndSaveUser("Resident A", "resA@aproova.com", Role.FLAT_MEMBER, flat101);
        User residentB = createAndSaveUser("Resident B", "resB@aproova.com", Role.FLAT_MEMBER, flat101);

        CreateWorkRequest req = new CreateWorkRequest();
        req.setTitle("CCTV Installation");
        req.setEstimatedCost(BigDecimal.valueOf(8000));
        req.setCategory("CCTV");

        WorkDto work = workService.createWork(req, residentA);

        workService.voteOnWork(work.getId(), "APPROVE", "First vote by Flat 101", residentA);

        assertThrows(BusinessRuleException.class, () -> {
            workService.voteOnWork(work.getId(), "REJECT", "Second vote attempt by Flat 101", residentB);
        });
    }

    @Test
    void testConcurrentVotesSameFlat_OnlyOneSucceeds() throws Exception {
        Flat flat101 = getOrCreateFlat("101_conc");
        User residentA = createAndSaveUser("Concurrent A", "concA@aproova.com", Role.FLAT_MEMBER, flat101);
        User residentB = createAndSaveUser("Concurrent B", "concB@aproova.com", Role.FLAT_MEMBER, flat101);

        CreateWorkRequest req = new CreateWorkRequest();
        req.setTitle("Roof Leak Repair");
        req.setEstimatedCost(BigDecimal.valueOf(4000));
        req.setCategory("REPAIR");

        WorkDto work = workService.createWork(req, residentA);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable voteTaskA = () -> {
            try {
                latch.await();
                workService.voteOnWork(work.getId(), "APPROVE", "YES by Resident A", residentA);
                successCount.incrementAndGet();
            } catch (Exception ignored) {}
        };

        Runnable voteTaskB = () -> {
            try {
                latch.await();
                workService.voteOnWork(work.getId(), "REJECT", "NO by Resident B", residentB);
                successCount.incrementAndGet();
            } catch (Exception ignored) {}
        };

        executor.submit(voteTaskA);
        executor.submit(voteTaskB);
        latch.countDown();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1, workVoteRepository.findByWorkId(work.getId()).size(), "Database must contain EXACTLY ONE vote for Flat 101 due to UNIQUE constraint.");
    }

    @Test
    @Transactional
    void testNineVotesMajorityApproval() {
        Flat creatorFlat = getOrCreateFlat("101");
        User creator = createAndSaveUser("Creator 101", "creator101@aproova.com", Role.FLAT_MEMBER, creatorFlat);

        CreateWorkRequest req = new CreateWorkRequest();
        req.setTitle("Lift Transformer Repair");
        req.setDescription("Overhaul lift motors");
        req.setEstimatedCost(BigDecimal.valueOf(12000));
        req.setCategory("LIFT");

        WorkDto work = workService.createWork(req, creator);
        assertNotNull(work);
        assertEquals("PENDING_APPROVAL", work.getStatus());

        List<String> flats = List.of("001", "002", "003", "004", "101", "102", "103", "104", "201");
        WorkDto updatedWork = null;

        for (String flatNum : flats) {
            Flat f = getOrCreateFlat(flatNum);
            User voter = createAndSaveUser("Resident " + flatNum, "voter" + flatNum + "@aproova.com", Role.FLAT_MEMBER, f);

            updatedWork = workService.voteOnWork(work.getId(), "APPROVE", "Approved by Flat " + flatNum, voter);
        }

        assertNotNull(updatedWork);
        assertEquals("APPROVED", updatedWork.getStatus());
        assertEquals(9, updatedWork.getAcceptCount());
        assertEquals(9, updatedWork.getMajorityRequired());
    }

    @Test
    @Transactional
    void testRepairApprovalDoesNotCreateExpenseOrAlterBalance() {
        long initialExpenseCount = expenseRepository.count();

        Flat creatorFlat = getOrCreateFlat("101");
        User creator = createAndSaveUser("Creator 101", "creator101_exp@aproova.com", Role.FLAT_MEMBER, creatorFlat);

        CreateWorkRequest req = new CreateWorkRequest();
        req.setTitle("Drainage Pipe Replacement");
        req.setEstimatedCost(BigDecimal.valueOf(15000));
        req.setCategory("DRAINAGE");

        WorkDto work = workService.createWork(req, creator);
        assertEquals(initialExpenseCount, expenseRepository.count(), "Repair creation must NOT create an expense.");

        List<String> flats = List.of("001", "002", "003", "004", "101", "102", "103", "104", "201");
        for (String flatNum : flats) {
            Flat f = getOrCreateFlat(flatNum);
            User voter = createAndSaveUser("Resident " + flatNum, "voter_exp_" + flatNum + "@aproova.com", Role.FLAT_MEMBER, f);
            workService.voteOnWork(work.getId(), "APPROVE", "Approved by Flat " + flatNum, voter);
        }

        WorkDto approvedWork = workService.getWorkById(work.getId());
        assertEquals("APPROVED", approvedWork.getStatus());
        assertEquals(initialExpenseCount, expenseRepository.count(), "Majority approval must NOT create an expense.");

        Flat maintFlat = getOrCreateFlat("001");
        User maintUser = createAndSaveUser("Maintenance User", "maint@aproova.com", Role.MAINTENANCE_FLAT, maintFlat);
        WorkDto respondedWork = workService.maintenanceRespond(work.getId(), "ACCEPTED", "Maintenance accepted task", maintUser);

        assertEquals("ACCEPTED_BY_MAINTENANCE", respondedWork.getStatus());
        assertEquals(initialExpenseCount, expenseRepository.count(), "Maintenance acceptance must NOT create an expense.");
    }
}
