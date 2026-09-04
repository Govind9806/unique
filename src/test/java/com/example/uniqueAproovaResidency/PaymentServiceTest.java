package com.example.uniqueAproovaResidency;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
import com.example.uniqueAproovaResidency.module.maintenance.repository.MaintenanceBillRepository;
import com.example.uniqueAproovaResidency.module.payment.dto.PaymentDto;
import com.example.uniqueAproovaResidency.module.payment.dto.PaymentInitiationResponse;
import com.example.uniqueAproovaResidency.module.payment.dto.WebhookPayload;
import com.example.uniqueAproovaResidency.module.payment.service.PaymentService;
import com.example.uniqueAproovaResidency.module.receipt.repository.ReceiptRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

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
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MaintenanceBillRepository billRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlatRepository flatRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    private MaintenanceBill pendingBill;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("admin@aproova.com").orElse(null);
        Flat flat302 = flatRepository.findById("flat-302").orElse(null);
        Flat flat101 = flatRepository.findById("flat-101").orElse(null);

        pendingBill = billRepository.findById("bill-302-08-2026").orElseGet(() -> {
            MaintenanceBill bill = MaintenanceBill.builder()
                    .id("bill-302-08-2026")
                    .flat(flat302)
                    .billingMonth(8)
                    .billingYear(2026)
                    .amount(BigDecimal.valueOf(2000))
                    .dueDate(LocalDate.now().plusDays(10))
                    .status("PENDING")
                    .build();
            return billRepository.save(bill);
        });

        billRepository.findById("bill-101-08-2026").orElseGet(() -> {
            MaintenanceBill bill = MaintenanceBill.builder()
                    .id("bill-101-08-2026")
                    .flat(flat101)
                    .billingMonth(8)
                    .billingYear(2026)
                    .amount(BigDecimal.valueOf(2000))
                    .dueDate(LocalDate.now().plusDays(10))
                    .status("PAID")
                    .build();
            return billRepository.save(bill);
        });
    }

    @Test
    void testInitiateBillPayment_CalculatesAuthoritativeAmountAndReturnsUpiUri() {
        assertNotNull(pendingBill);
        PaymentInitiationResponse response = paymentService.initiateBillPayment(pendingBill.getId(), testUser);

        assertNotNull(response);
        assertEquals(pendingBill.getId(), response.getBillId());
        assertEquals(0, response.getAmount().compareTo(BigDecimal.valueOf(2000)));
        assertTrue(response.getUpiUri().startsWith("upi://pay"));
        assertTrue(response.getUpiUri().contains("pa=aproovaresidency%40upi") || response.getUpiUri().contains("pa=aproovaresidency@upi"));
    }

    @Test
    void testInitiateBillPayment_AlreadyPaidBill_ThrowsException() {
        MaintenanceBill paidBill = billRepository.findById("bill-101-08-2026").orElse(null);
        assertNotNull(paidBill);

        assertThrows(BusinessRuleException.class, () -> paymentService.initiateBillPayment(paidBill.getId(), testUser));
    }

    @Test
    void testProcessWebhook_SuccessfulPayment_UpdatesBill_CreatesReceipt_RecordsLedgerIncome() {
        PaymentInitiationResponse initResponse = paymentService.initiateBillPayment(pendingBill.getId(), testUser);

        WebhookPayload webhook = WebhookPayload.builder()
                .merchantReference(initResponse.getMerchantReference())
                .providerTransactionId("UPI/BANK/9988776655")
                .amount(BigDecimal.valueOf(2000))
                .status("SUCCESS")
                .build();

        PaymentDto result = paymentService.processWebhook(webhook, null);

        assertEquals("SUCCESS", result.getStatus());
        assertEquals("PAID", billRepository.findById(pendingBill.getId()).get().getStatus());

        // Verify receipt created
        assertNotNull(receiptRepository.findByPaymentId(result.getId()).orElse(null));

        // Verify ledger income recorded
        assertTrue(ledgerRepository.existsByReferenceTypeAndReferenceId("PAYMENT", result.getId()));
    }

    @Test
    void testProcessWebhook_Idempotency_DuplicateDeliveriesProcessedOnce() {
        PaymentInitiationResponse initResponse = paymentService.initiateBillPayment(pendingBill.getId(), testUser);

        WebhookPayload webhook = WebhookPayload.builder()
                .merchantReference(initResponse.getMerchantReference())
                .providerTransactionId("UPI/BANK/IDEMPOTENT-001")
                .amount(BigDecimal.valueOf(2000))
                .status("SUCCESS")
                .build();

        long ledgerCountBefore = ledgerRepository.count();

        // Deliver the same successful webhook 5 times sequentially
        PaymentDto res1 = paymentService.processWebhook(webhook, null);
        PaymentDto res2 = paymentService.processWebhook(webhook, null);
        PaymentDto res3 = paymentService.processWebhook(webhook, null);
        PaymentDto res4 = paymentService.processWebhook(webhook, null);
        PaymentDto res5 = paymentService.processWebhook(webhook, null);

        // All 5 responses return SUCCESS payment DTO
        assertEquals("SUCCESS", res1.getStatus());
        assertEquals("SUCCESS", res5.getStatus());

        // EXACTLY ONE ledger income entry created (ledger count increased by 1, not 5!)
        long ledgerCountAfter = ledgerRepository.count();
        assertEquals(ledgerCountBefore + 1, ledgerCountAfter);
    }

    @Test
    void testProcessWebhook_AmountMismatch_RejectsPayment() {
        PaymentInitiationResponse initResponse = paymentService.initiateBillPayment(pendingBill.getId(), testUser);

        WebhookPayload tamperWebhook = WebhookPayload.builder()
                .merchantReference(initResponse.getMerchantReference())
                .providerTransactionId("UPI/TAMPERED")
                .amount(BigDecimal.valueOf(1)) // Tampered amount ₹1 instead of ₹2,000
                .status("SUCCESS")
                .build();

        assertThrows(BusinessRuleException.class, () -> paymentService.processWebhook(tamperWebhook, null));
    }
}
