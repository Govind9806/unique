package com.example.uniqueAproovaResidency.module.payment.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.history.repository.ApartmentHistoryRepository;
import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import com.example.uniqueAproovaResidency.module.ledger.repository.LedgerRepository;
import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
import com.example.uniqueAproovaResidency.module.maintenance.repository.MaintenanceBillRepository;
import com.example.uniqueAproovaResidency.module.maintenance.service.BillService;
import com.example.uniqueAproovaResidency.module.notification.entity.Notification;
import com.example.uniqueAproovaResidency.module.notification.repository.NotificationRepository;
import com.example.uniqueAproovaResidency.module.payment.dto.*;
import com.example.uniqueAproovaResidency.module.payment.entity.Payment;
import com.example.uniqueAproovaResidency.module.payment.provider.PaymentProvider;
import com.example.uniqueAproovaResidency.module.payment.provider.PaymentProviderFactory;
import com.example.uniqueAproovaResidency.module.payment.repository.PaymentRepository;
import com.example.uniqueAproovaResidency.module.receipt.dto.ReceiptDto;
import com.example.uniqueAproovaResidency.module.receipt.entity.Receipt;
import com.example.uniqueAproovaResidency.module.receipt.repository.ReceiptRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MaintenanceBillRepository billRepository;
    private final ReceiptRepository receiptRepository;
    private final LedgerRepository ledgerRepository;
    private final NotificationRepository notificationRepository;
    private final ApartmentHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final PaymentProviderFactory paymentProviderFactory;
    private final FlatRepository flatRepository;
    private final BillService billService;

    @Value("${app.payment.upi-merchant-vpa:aproovaresidency@upi}")
    private String merchantVpa;

    @Value("${app.payment.upi-merchant-name:Aproova Residency Society}")
    private String merchantName;

    @Value("${app.payment.webhook-secret:aproova_webhook_secret_key_2026}")
    private String webhookSecret;

    @Transactional
    public PaymentInitiationResponse createPaymentOrder(CreateOrderRequest request, User user) {
        log.info("Creating payment order for request: {}, User: {}", request, user != null ? user.getEmail() : "anonymous");

        if (user == null) {
            throw new BusinessRuleException("UNAUTHORIZED_RESIDENT", "Authenticated resident is required to initiate payments.");
        }

        String userRole = user.getRole() != null ? user.getRole().name().toUpperCase() : "";
        boolean isAdminOrTreasurer = userRole.contains("ADMIN") || userRole.contains("TREASURER");

        MaintenanceBill bill = null;
        Flat targetFlat = user.getFlat();
        BigDecimal authoritativeAmount;

        if (request.getBillId() != null && !request.getBillId().isBlank()) {
            bill = billRepository.findById(request.getBillId())
                    .orElseThrow(() -> new ResourceNotFoundException("MaintenanceBill", "id", request.getBillId()));

            targetFlat = bill.getFlat();

            // Authorization & Anti-Spoofing Check: User's flat MUST match bill's flat if not Admin/Treasurer
            if (user.getFlat() != null && !bill.getFlat().getId().equals(user.getFlat().getId()) && !isAdminOrTreasurer) {
                throw new BusinessRuleException("FLAT_SPOOFING_PROHIBITED", "You are not authorized to pay bills for another flat.");
            }

            if ("PAID".equalsIgnoreCase(bill.getStatus())) {
                throw new BusinessRuleException("BILL_ALREADY_PAID", "This maintenance bill has already been paid.");
            }
            authoritativeAmount = bill.getAmount();
        } else {
            if (targetFlat == null && request.getFlatNumber() != null) {
                String cleanNum = request.getFlatNumber().replaceAll("flat-", "");
                targetFlat = flatRepository.findByFlatNumber(cleanNum)
                        .orElseGet(() -> flatRepository.findById("flat-" + cleanNum)
                                .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatNumber", request.getFlatNumber())));
            }

            if (targetFlat == null) {
                throw new BusinessRuleException("FLAT_REQUIRED", "Flat context is required to initiate dues payment.");
            }

            // Authorization Check for dues payment
            if (user.getFlat() != null && !targetFlat.getId().equals(user.getFlat().getId()) && !isAdminOrTreasurer) {
                throw new BusinessRuleException("FLAT_SPOOFING_PROHIBITED", "You are not authorized to pay dues for another flat.");
            }

            // Flat Dues Order Creation: Authoritative DB Calculation
            var dues = billService.getFlatDuesSummary(targetFlat.getFlatNumber());
            String mode = request.getPaymentMode() != null ? request.getPaymentMode().toUpperCase() : "CURRENT_MONTH";

            if ("PAST_ARREARS".equals(mode)) {
                authoritativeAmount = dues.getPreviousPendingArrears();
            } else if ("ALL_DUES".equals(mode)) {
                authoritativeAmount = dues.getOverallPendingAmount();
            } else {
                authoritativeAmount = dues.getTotalMonthlyBill();
            }

            if (authoritativeAmount == null || authoritativeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("NO_PENDING_DUES", "Flat " + targetFlat.getFlatNumber() + " has no pending dues for " + mode + ".");
            }
        }

        // Amount Tamper Prevention
        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (request.getAmount().compareTo(authoritativeAmount) != 0) {
                log.warn("Client submitted amount ({}) differs from authoritative amount ({}). Enforcing authoritative amount.", request.getAmount(), authoritativeAmount);
            }
        }

        String merchantReference = "REF-APROOVA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PaymentProvider provider = paymentProviderFactory.getProvider();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .bill(bill)
                .flat(targetFlat)
                .amount(authoritativeAmount)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI_INTENT")
                .provider(provider.getProviderName())
                .gatewayOrderId(merchantReference)
                .status("INITIATED")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        String note = "Flat " + targetFlat.getFlatNumber() + " Maintenance Payment (" + merchantReference + ")";

        log.info("Payment order created cleanly. ID: {}, Ref: {}, Amount: ₹{}", savedPayment.getId(), merchantReference, authoritativeAmount);
        return provider.initiatePayment(savedPayment, merchantVpa, merchantName, note);
    }

    @Transactional
    public PaymentInitiationResponse initiateBillPayment(String billId, User user) {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setBillId(billId);
        return createPaymentOrder(req, user);
    }

    @Transactional
    public PaymentDto processWebhook(WebhookPayload payload, String rawSignature) {
        log.info("Processing webhook for merchantReference: {}", payload.getMerchantReference());

        PaymentProvider provider = paymentProviderFactory.getProvider();
        PaymentVerificationResult result = provider.processWebhook(payload, rawSignature, webhookSecret);

        Payment payment = paymentRepository.findByGatewayOrderId(payload.getMerchantReference())
                .orElseGet(() -> paymentRepository.findById(payload.getMerchantReference())
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", "reference", payload.getMerchantReference())));

        // IDEMPOTENCY CHECK: If already SUCCESS, return immediately without re-processing!
        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            log.info("Payment reference {} already processed as SUCCESS. Skipping duplicate webhook execution.", payment.getGatewayOrderId());
            return PaymentDto.fromEntity(payment);
        }

        // Amount & Reference Validation
        if (payload.getAmount() != null && payload.getAmount().compareTo(payment.getAmount()) != 0) {
            log.error("Amount mismatch for payment {}: expected {}, received {}", payment.getId(), payment.getAmount(), payload.getAmount());
            payment.setStatus("FAILED");
            payment.setFailureReason("Amount mismatch in webhook confirmation");
            paymentRepository.save(payment);
            throw new BusinessRuleException("PAYMENT_AMOUNT_MISMATCH", "Confirmed amount does not match authoritative bill amount");
        }

        // Currency Validation
        if (payload.getCurrency() != null && !"INR".equalsIgnoreCase(payload.getCurrency())) {
            log.error("Currency mismatch for payment {}: expected INR, received {}", payment.getId(), payload.getCurrency());
            payment.setStatus("FAILED");
            payment.setFailureReason("Currency mismatch in webhook confirmation");
            paymentRepository.save(payment);
            throw new BusinessRuleException("INVALID_CURRENCY", "Currency must be INR");
        }

        if (result.isSuccessful()) {
            payment.setGatewayPaymentId(result.getProviderTransactionId());
            payment.setTransactionReference(result.getProviderTransactionId());
            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);

            // 1. Mark Bill PAID
            MaintenanceBill bill = payment.getBill();
            if (bill != null) {
                bill.setStatus("PAID");
                billRepository.save(bill);
            }

            // 2. Generate Receipt
            createReceiptForPayment(savedPayment, "Resident");

            // 3. Create Immutable Ledger INCOME (Strict Idempotency Check)
            if (!ledgerRepository.existsByReferenceTypeAndReferenceId("PAYMENT", savedPayment.getId())) {
                LedgerTransaction ledgerTx = LedgerTransaction.builder()
                        .id(UUID.randomUUID().toString())
                        .type("INCOME")
                        .category("MAINTENANCE_FEE")
                        .amount(savedPayment.getAmount())
                        .description("Flat " + savedPayment.getFlat().getFlatNumber() + " Maintenance Payment")
                        .referenceType("PAYMENT")
                        .referenceId(savedPayment.getId())
                        .transactionDate(LocalDateTime.now())
                        .build();
                ledgerRepository.save(ledgerTx);
            }

            // 4. Resident Notification
            User resident = userRepository.findByFlatId(savedPayment.getFlat().getId()).stream().findFirst().orElse(null);
            if (resident != null) {
                Notification notification = Notification.builder()
                        .id(UUID.randomUUID().toString())
                        .user(resident)
                        .type("PAYMENT_SUCCESS")
                        .title("Maintenance Payment Received")
                        .message("Your maintenance payment of Rs. " + savedPayment.getAmount() + " for Flat " + savedPayment.getFlat().getFlatNumber() + " was confirmed successfully.")
                        .referenceType("PAYMENT")
                        .referenceId(savedPayment.getId())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
            }

            return PaymentDto.fromEntity(savedPayment);
        } else {
            payment.setStatus("FAILED");
            payment.setFailureReason(result.getFailureReason() != null ? result.getFailureReason() : "Payment rejected by provider");
            Payment savedPayment = paymentRepository.save(payment);
            return PaymentDto.fromEntity(savedPayment);
        }
    }

    @Transactional
    public PaymentDto verifyPaymentStatus(String paymentId, String gatewayPaymentId, User user) {
        log.info("Verifying payment status for ID: {}, GatewayPaymentId: {}, User: {}", paymentId, gatewayPaymentId, user != null ? user.getEmail() : "anonymous");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseGet(() -> paymentRepository.findByGatewayOrderId(paymentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId)));

        // Anti-Spoofing Authorization Guard
        if (user != null && user.getFlat() != null) {
            String roleName = user.getRole() != null ? user.getRole().name().toUpperCase() : "";
            boolean isAdminOrTreasurer = roleName.contains("ADMIN") || roleName.contains("TREASURER");
            if (!payment.getFlat().getId().equals(user.getFlat().getId()) && !isAdminOrTreasurer) {
                throw new BusinessRuleException("FLAT_SPOOFING_PROHIBITED", "You cannot verify payments belonging to another flat.");
            }
        }

        // IDEMPOTENCY CHECK: If already SUCCESS, return immediately without duplicate ledger credits!
        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            log.info("Payment {} is already confirmed SUCCESS. Skipping duplicate ledger transaction.", payment.getId());
            return PaymentDto.fromEntity(payment);
        }

        PaymentProvider provider = paymentProviderFactory.getProvider();
        String txnRef = (gatewayPaymentId != null && !gatewayPaymentId.isBlank()) ? gatewayPaymentId : payment.getGatewayPaymentId();
        PaymentVerificationResult result = provider.verifyPayment(txnRef, payment.getGatewayOrderId());

        if (result.getAmount() != null && result.getAmount().compareTo(payment.getAmount()) != 0) {
            log.error("Amount mismatch for payment {}: expected {}, received {}", payment.getId(), payment.getAmount(), result.getAmount());
            payment.setStatus("FAILED");
            payment.setFailureReason("Amount mismatch in verification confirmation");
            paymentRepository.save(payment);
            throw new BusinessRuleException("PAYMENT_AMOUNT_MISMATCH", "Confirmed amount does not match authoritative bill amount");
        }

        if (result.getCurrency() != null && !"INR".equalsIgnoreCase(result.getCurrency())) {
            log.error("Currency mismatch for payment {}: expected INR, received {}", payment.getId(), result.getCurrency());
            payment.setStatus("FAILED");
            payment.setFailureReason("Currency mismatch in verification confirmation");
            paymentRepository.save(payment);
            throw new BusinessRuleException("INVALID_CURRENCY", "Currency must be INR");
        }

        if (result.isSuccessful()) {
            payment.setGatewayPaymentId(txnRef != null ? txnRef : "UPI-TXN-" + System.currentTimeMillis());
            payment.setTransactionReference(payment.getGatewayPaymentId());
            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);

            // Mark Bill PAID
            if (savedPayment.getBill() != null) {
                MaintenanceBill bill = savedPayment.getBill();
                bill.setStatus("PAID");
                billRepository.save(bill);
            }

            // Generate Digital Receipt
            createReceiptForPayment(savedPayment, user != null ? user.getName() : "Resident Flat " + savedPayment.getFlat().getFlatNumber());

            // Create Immutable Ledger INCOME (Strict Idempotency Guard)
            if (!ledgerRepository.existsByReferenceTypeAndReferenceId("PAYMENT", savedPayment.getId())) {
                LedgerTransaction ledgerTx = LedgerTransaction.builder()
                        .id(UUID.randomUUID().toString())
                        .type("INCOME")
                        .category("MAINTENANCE_FEE")
                        .amount(savedPayment.getAmount())
                        .description("Flat " + savedPayment.getFlat().getFlatNumber() + " Maintenance Payment")
                        .referenceType("PAYMENT")
                        .referenceId(savedPayment.getId())
                        .transactionDate(LocalDateTime.now())
                        .build();
                ledgerRepository.save(ledgerTx);
            }

            return PaymentDto.fromEntity(savedPayment);
        } else {
            payment.setStatus("FAILED");
            payment.setFailureReason(result.getFailureReason() != null ? result.getFailureReason() : "Verification failed or cancelled by user");
            Payment savedPayment = paymentRepository.save(payment);
            return PaymentDto.fromEntity(savedPayment);
        }
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseGet(() -> paymentRepository.findByGatewayOrderId(paymentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId)));
        return PaymentDto.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getMyPayments(String flatId) {
        if (flatId == null) {
            return List.of();
        }
        return paymentRepository.findByFlatIdOrderByPaidAtDesc(flatId).stream()
                .map(PaymentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsForFlatNumber(String flatNumber) {
        log.info("Fetching payment history for flatNumber: {}", flatNumber);
        String target = flatNumber.toLowerCase().startsWith("flat-") ? flatNumber.substring(5) : flatNumber;
        List<Payment> list = paymentRepository.findByFlatFlatNumberOrderByPaidAtDesc(target);
        if (list.isEmpty()) {
            list = paymentRepository.findByFlatIdOrderByPaidAtDesc("flat-" + target);
        }
        return list.stream().map(PaymentDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPayments() {
        log.info("Fetching all apartment payments history directory");
        return paymentRepository.findAllByOrderByPaidAtDesc().stream()
                .map(PaymentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceiptByPaymentId(String paymentId) {
        Receipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "paymentId", paymentId));
        return ReceiptDto.fromEntity(receipt);
    }

    @Transactional
    public PaymentDto refundPayment(String paymentId, String reason, User admin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new BusinessRuleException("CANNOT_REFUND", "Only successful payments can be refunded");
        }

        PaymentProvider provider = paymentProviderFactory.getProvider();
        boolean refunded = provider.refundPayment(payment, reason);

        if (refunded) {
            payment.setStatus("REFUNDED");
            payment.setFailureReason("Refunded: " + reason);
            Payment saved = paymentRepository.save(payment);

            // Reversal ledger transaction
            LedgerTransaction reversal = LedgerTransaction.builder()
                    .id(UUID.randomUUID().toString())
                    .type("EXPENSE")
                    .category("PAYMENT_REFUND")
                    .amount(payment.getAmount())
                    .description("Refund for Flat " + payment.getFlat().getFlatNumber() + " Payment: " + reason)
                    .referenceType("PAYMENT_REFUND")
                    .referenceId(payment.getId())
                    .transactionDate(LocalDateTime.now())
                    .createdBy(admin)
                    .build();
            ledgerRepository.save(reversal);

            return PaymentDto.fromEntity(saved);
        }
        throw new BusinessRuleException("REFUND_FAILED", "Provider failed to refund payment");
    }

    private Receipt createReceiptForPayment(Payment payment, String residentName) {
        String receiptNumber = "RCP-" + System.currentTimeMillis();
        String period = payment.getBill() != null
                ? payment.getBill().getBillingMonth() + "/" + payment.getBill().getBillingYear()
                : "Maintenance Fee";

        Receipt receipt = Receipt.builder()
                .id(UUID.randomUUID().toString())
                .receiptNumber(receiptNumber)
                .payment(payment)
                .flat(payment.getFlat())
                .residentName(residentName)
                .billPeriod(period)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionReference() != null ? payment.getTransactionReference() : payment.getGatewayPaymentId())
                .paidDate(payment.getPaidAt() != null ? payment.getPaidAt() : LocalDateTime.now())
                .pdfUrl("/api/v1/payments/" + payment.getId() + "/receipt/pdf")
                .build();

        return receiptRepository.save(receipt);
    }

    @Transactional
    public PaymentDto recordCashPayment(RecordCashPaymentRequest request, User recorder) {
        log.info("Recording offline cash payment for flat: {}, amount: {}", request.getFlatNumber(), request.getAmount());

        String rawFlat = request.getFlatNumber() != null ? request.getFlatNumber().trim() : "101";
        String cleanFlatNumber = rawFlat.toLowerCase().startsWith("flat-") ? rawFlat.substring(5) : rawFlat;

        Flat flat = flatRepository.findByFlatNumber(cleanFlatNumber)
                .orElseGet(() -> flatRepository.findById("flat-" + cleanFlatNumber)
                        .orElseGet(() -> flatRepository.findAll().stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatNumber", cleanFlatNumber))));

        BigDecimal amount = request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? request.getAmount()
                : BigDecimal.valueOf(1250);

        String paymentId = "CASH-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Mark unpaid maintenance bill for flat as PAID if exists
        MaintenanceBill bill = billRepository.findByFlatIdAndStatus(flat.getId(), "UNPAID")
                .stream().findFirst().orElse(null);

        if (bill != null) {
            bill.setStatus("PAID");
            billRepository.save(bill);
        }

        Payment payment = Payment.builder()
                .id(paymentId)
                .flat(flat)
                .bill(bill)
                .amount(amount)
                .paymentMethod("CASH")
                .status("SUCCESS")
                .gatewayOrderId("CASH-ORD-" + System.currentTimeMillis())
                .gatewayPaymentId(paymentId)
                .transactionReference("CASH-REF-" + System.currentTimeMillis())
                .paidAt(LocalDateTime.now())
                .failureReason(request.getRemarks() != null ? "CASH RECEIVED: " + request.getRemarks() : "Cash Payment Verified")
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Generate Digital Cash Receipt
        String residentName = "Resident Flat " + cleanFlatNumber;
        createReceiptForPayment(savedPayment, residentName);

        // Record Immutable Ledger INCOME for live treasury balance
        LedgerTransaction ledgerTx = LedgerTransaction.builder()
                .id(UUID.randomUUID().toString())
                .type("INCOME")
                .category("CASH_MAINTENANCE_PAYMENT")
                .amount(savedPayment.getAmount())
                .description("Flat " + cleanFlatNumber + " Cash Maintenance Payment" + (request.getRemarks() != null && !request.getRemarks().isBlank() ? " (" + request.getRemarks() + ")" : ""))
                .referenceType("PAYMENT")
                .referenceId(savedPayment.getId())
                .transactionDate(LocalDateTime.now())
                .createdBy(recorder)
                .build();
        ledgerRepository.save(ledgerTx);

        // Send Notification to Flat Resident
        List<User> residents = userRepository.findByFlatId(flat.getId());
        for (User resident : residents) {
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .user(resident)
                    .type("CASH_PAYMENT_VERIFIED")
                    .title("💵 Cash Payment Received & Confirmed")
                    .message("Cash maintenance payment of ₹" + savedPayment.getAmount() + " for Flat " + cleanFlatNumber + " has been recorded and verified by Maintenance. Digital receipt generated.")
                    .referenceType("PAYMENT")
                    .referenceId(savedPayment.getId())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }

        return PaymentDto.fromEntity(savedPayment);
    }
}
