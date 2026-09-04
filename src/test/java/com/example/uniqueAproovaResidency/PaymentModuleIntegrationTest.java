package com.example.uniqueAproovaResidency;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
import com.example.uniqueAproovaResidency.module.maintenance.repository.MaintenanceBillRepository;
import com.example.uniqueAproovaResidency.module.payment.dto.CreateOrderRequest;
import com.example.uniqueAproovaResidency.module.payment.dto.PaymentDto;
import com.example.uniqueAproovaResidency.module.payment.dto.PaymentInitiationResponse;
import com.example.uniqueAproovaResidency.module.payment.entity.Payment;
import com.example.uniqueAproovaResidency.module.payment.repository.PaymentRepository;
import com.example.uniqueAproovaResidency.module.payment.service.PaymentService;
import com.example.uniqueAproovaResidency.module.role.Role;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
@Transactional
public class PaymentModuleIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MaintenanceBillRepository billRepository;

    @Autowired
    private FlatRepository flatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    private Flat flat101;
    private Flat flat102;
    private User resident101;
    private User resident102;
    private MaintenanceBill bill101;
    private MaintenanceBill bill102;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        billRepository.deleteAll();

        flat101 = flatRepository.findByFlatNumber("101")
                .orElseGet(() -> flatRepository.save(Flat.builder().id("flat-101").flatNumber("101").floor(1).status("OCCUPIED").build()));

        flat102 = flatRepository.findByFlatNumber("102")
                .orElseGet(() -> flatRepository.save(Flat.builder().id("flat-102").flatNumber("102").floor(1).status("OCCUPIED").build()));

        resident101 = userRepository.findByEmail("resident101_test@aproova.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .id(UUID.randomUUID().toString())
                        .name("Resident 101")
                        .email("resident101_test@aproova.com")
                        .phone("9876543210")
                        .password("hashed_pass")
                        .role(Role.FLAT_MEMBER)
                        .flat(flat101)
                        .build()));

        resident102 = userRepository.findByEmail("resident102_test@aproova.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .id(UUID.randomUUID().toString())
                        .name("Resident 102")
                        .email("resident102_test@aproova.com")
                        .phone("9876543211")
                        .password("hashed_pass")
                        .role(Role.FLAT_MEMBER)
                        .flat(flat102)
                        .build()));

        bill101 = billRepository.save(MaintenanceBill.builder()
                .id(UUID.randomUUID().toString())
                .flat(flat101)
                .amount(BigDecimal.valueOf(1250))
                .billingMonth(8)
                .billingYear(2026)
                .dueDate(LocalDate.of(2026, 8, 31))
                .status("PENDING")
                .build());

        bill102 = billRepository.save(MaintenanceBill.builder()
                .id(UUID.randomUUID().toString())
                .flat(flat102)
                .amount(BigDecimal.valueOf(1250))
                .billingMonth(8)
                .billingYear(2026)
                .dueDate(LocalDate.of(2026, 8, 31))
                .status("PENDING")
                .build());
    }

    @Test
    @DisplayName("1. Order Creation starts as INITIATED with 0 Ledger Change")
    void testCreatePaymentOrder_StartsAsInitiated_ZeroLedgerChange() {
        long initialLedgerCount = ledgerRepository.count();

        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        req.setPaymentMethod("UPI_INTENT");

        PaymentInitiationResponse response = paymentService.createPaymentOrder(req, resident101);

        assertNotNull(response);
        assertNotNull(response.getPaymentId());
        assertNotNull(response.getMerchantReference());
        assertTrue(response.getMerchantReference().startsWith("REF-APROOVA-"));
        assertTrue(response.getUpiUri().contains("upi://pay"));

        Payment payment = paymentRepository.findById(response.getPaymentId()).orElse(null);
        assertNotNull(payment);
        assertEquals("INITIATED", payment.getStatus());

        // Zero Ledger Change Invariant
        assertEquals(initialLedgerCount, ledgerRepository.count(), "Initiated payment must NOT alter ledger balance!");
    }

    @Test
    @DisplayName("2. Anti-Spoofing Authorization Guard: Resident 101 cannot pay for Flat 102 Bill")
    void testCreatePaymentOrder_Authorization_FlatSpoofingRejected() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill102.getId()); // Flat 102 bill

        // Resident 101 attempts to pay Flat 102 bill -> Rejected!
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                paymentService.createPaymentOrder(req, resident101)
        );
        assertEquals("FLAT_SPOOFING_PROHIBITED", ex.getCode());
    }

    @Test
    @DisplayName("3. Verified SUCCESS credits ledger exactly once and marks Bill PAID")
    void testVerifyPayment_Success_CreditsLedgerOnce() {
        long initialLedgerCount = ledgerRepository.count();

        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        PaymentInitiationResponse initResp = paymentService.createPaymentOrder(req, resident101);

        // Verification of SUCCESS
        String providerTxnId = "BANK-UTR-999111";
        PaymentDto verifiedDto = paymentService.verifyPaymentStatus(initResp.getPaymentId(), providerTxnId, resident101);

        assertNotNull(verifiedDto);
        assertEquals("SUCCESS", verifiedDto.getStatus());

        // Verify Maintenance Bill updated to PAID
        MaintenanceBill updatedBill = billRepository.findById(bill101.getId()).orElseThrow();
        assertEquals("PAID", updatedBill.getStatus());

        // Verify Income Ledger Entry Created
        List<LedgerTransaction> ledgerList = ledgerRepository.findAll();
        assertEquals(initialLedgerCount + 1, ledgerList.size());
        LedgerTransaction tx = ledgerList.get(ledgerList.size() - 1);
        assertEquals("INCOME", tx.getType());
        assertEquals("MAINTENANCE_FEE", tx.getCategory());
        assertEquals(BigDecimal.valueOf(1250), tx.getAmount());
    }

    @Test
    @DisplayName("4. Strict Idempotency Check: Second call to verifyPaymentStatus returns without double-crediting ledger")
    void testVerifyPayment_DuplicateCall_Idempotency() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        PaymentInitiationResponse initResp = paymentService.createPaymentOrder(req, resident101);

        // First verification
        paymentService.verifyPaymentStatus(initResp.getPaymentId(), "BANK-UTR-001", resident101);
        long ledgerCountAfterFirstVerify = ledgerRepository.count();

        // Second duplicate verification trigger
        PaymentDto secondDto = paymentService.verifyPaymentStatus(initResp.getPaymentId(), "BANK-UTR-001", resident101);

        assertEquals("SUCCESS", secondDto.getStatus());
        assertEquals(ledgerCountAfterFirstVerify, ledgerRepository.count(), "Duplicate verification MUST NOT double-credit ledger balance!");
    }

    @Test
    @DisplayName("5. Verification Failure sets state to FAILED with 0 Ledger Change")
    void testVerifyPayment_Failure_ZeroLedgerChange() {
        long initialLedgerCount = ledgerRepository.count();

        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        PaymentInitiationResponse initResp = paymentService.createPaymentOrder(req, resident101);

        // Verify with invalid/empty transaction reference -> FAILED
        PaymentDto failedDto = paymentService.verifyPaymentStatus(initResp.getPaymentId(), "", resident101);

        assertNotNull(failedDto);
        assertEquals("FAILED", failedDto.getStatus());

        // Bill remains PENDING
        MaintenanceBill updatedBill = billRepository.findById(bill101.getId()).orElseThrow();
        assertEquals("PENDING", updatedBill.getStatus());

        // Ledger count remains unchanged
        assertEquals(initialLedgerCount, ledgerRepository.count(), "Failed payment must NOT credit ledger balance!");
    }

    @Test
    @DisplayName("6. Authoritative Backend Amount Calculation: Client amount tampering is overridden")
    void testAmountTampering_AuthoritativeAmountEnforced() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        req.setAmount(BigDecimal.valueOf(1)); // Client tries to pay ₹1 for ₹1250 bill

        PaymentInitiationResponse initResp = paymentService.createPaymentOrder(req, resident101);

        // Server overrides client input with authoritative bill amount ₹1250
        assertEquals(BigDecimal.valueOf(1250), initResp.getAmount());
    }

    @Test
    @DisplayName("7. Webhook Amount Mismatch Rejection: Webhook payload with mismatching amount does not credit ledger")
    void testWebhook_AmountMismatch_RejectsAndDoesNotCredit() {
        long initialLedgerCount = ledgerRepository.count();

        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        PaymentInitiationResponse initResp = paymentService.createPaymentOrder(req, resident101);

        com.example.uniqueAproovaResidency.module.payment.dto.WebhookPayload webhook =
                com.example.uniqueAproovaResidency.module.payment.dto.WebhookPayload.builder()
                        .merchantReference(initResp.getMerchantReference())
                        .providerTransactionId("BANK-TXN-BAD-AMT")
                        .amount(BigDecimal.valueOf(100)) // Mismatching amount
                        .currency("INR")
                        .status("SUCCESS")
                        .build();

        assertThrows(BusinessRuleException.class, () -> paymentService.processWebhook(webhook, "raw_sig"));
        assertEquals(initialLedgerCount, ledgerRepository.count(), "Amount mismatch MUST NOT credit ledger balance!");
    }

    @Test
    @DisplayName("8. Webhook Currency Mismatch Rejection: Webhook payload with non-INR currency does not credit ledger")
    void testWebhook_CurrencyMismatch_RejectsAndDoesNotCredit() {
        long initialLedgerCount = ledgerRepository.count();

        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        PaymentInitiationResponse initResp = paymentService.createPaymentOrder(req, resident101);

        com.example.uniqueAproovaResidency.module.payment.dto.WebhookPayload webhook =
                com.example.uniqueAproovaResidency.module.payment.dto.WebhookPayload.builder()
                        .merchantReference(initResp.getMerchantReference())
                        .providerTransactionId("BANK-TXN-BAD-CURR")
                        .amount(BigDecimal.valueOf(1250))
                        .currency("USD") // Non-INR currency
                        .status("SUCCESS")
                        .build();

        assertThrows(BusinessRuleException.class, () -> paymentService.processWebhook(webhook, "raw_sig"));
        assertEquals(initialLedgerCount, ledgerRepository.count(), "Currency mismatch MUST NOT credit ledger balance!");
    }

    @Test
    @DisplayName("9. Multi-Call Verification Idempotency: 4 verification attempts credit ledger EXACTLY once")
    void testConcurrentVerification_GuaranteesSingleLedgerEntry() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(bill101.getId());
        PaymentInitiationResponse initResp = paymentService.createPaymentOrder(req, resident101);

        for (int i = 0; i < 4; i++) {
            paymentService.verifyPaymentStatus(initResp.getPaymentId(), "UTR-VERIFY-" + i, resident101);
        }

        // Authoritative Assertions: Payment SUCCESS, Bill PAID, exactly ONE Ledger Entry
        Payment payment = paymentRepository.findById(initResp.getPaymentId()).orElseThrow();
        assertEquals("SUCCESS", payment.getStatus());

        long paymentLedgerEntries = ledgerRepository.findAll().stream()
                .filter(t -> "PAYMENT".equals(t.getReferenceType()) && payment.getId().equals(t.getReferenceId()))
                .count();

        assertEquals(1, paymentLedgerEntries, "Multiple verification triggers MUST yield exactly 1 INCOME ledger entry!");
    }
}
