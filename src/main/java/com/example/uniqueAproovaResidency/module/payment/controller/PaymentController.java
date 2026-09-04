package com.example.uniqueAproovaResidency.module.payment.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.payment.dto.*;
import com.example.uniqueAproovaResidency.module.payment.service.PaymentService;
import com.example.uniqueAproovaResidency.module.receipt.dto.ReceiptDto;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Payment & Receipt", description = "Endpoints for UPI payment initiation, server-side webhook verification, payment status, and receipt generation")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping("/bills/{billId}/pay")
    @Operation(summary = "Initiate UPI Payment for Bill")
    public ResponseEntity<ApiResponse<PaymentInitiationResponse>> initiatePaymentForBill(
            @PathVariable String billId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/bills/{}/pay] -> User ID: {}", billId, currentUser.getId());
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        PaymentInitiationResponse response = paymentService.initiateBillPayment(billId, user);
        return ResponseEntity.ok(ApiResponse.success("UPI payment initiated", response));
    }

    @PostMapping("/payments/create-order")
    @Operation(summary = "Initiate Payment Order for Maintenance Dues / Bill")
    public ResponseEntity<ApiResponse<PaymentInitiationResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("REST REQUEST [POST /api/v1/payments/create-order] -> Bill ID: {}, Flat: {}", request.getBillId(), request.getFlatNumber());
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        PaymentInitiationResponse response = paymentService.createPaymentOrder(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment order created", response));
    }

    @PostMapping("/payments/{id}/verify")
    @Operation(summary = "Verify Payment Status with Gateway after UPI Return")
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(
            @PathVariable String id,
            @RequestBody(required = false) VerifyPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/payments/{}/verify]", id);
        User user = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        String gatewayPaymentId = request != null ? request.getGatewayPaymentId() : null;
        PaymentDto dto = paymentService.verifyPaymentStatus(id, gatewayPaymentId, user);
        return ResponseEntity.ok(ApiResponse.success("Payment status verified", dto));
    }

    @PostMapping("/payments/webhook")
    @Operation(summary = "Server-Side Payment Provider / Bank Webhook (Strict Idempotency)")
    public ResponseEntity<ApiResponse<PaymentDto>> handleWebhook(
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @Valid @RequestBody WebhookPayload payload) {
        log.info("REST REQUEST [POST /api/v1/payments/webhook] -> Ref: {}", payload.getMerchantReference());
        PaymentDto dto = paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook confirmed and processed", dto));
    }

    @GetMapping("/payments/{id}")
    @Operation(summary = "Get Authoritative Payment Details")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/payments/{}]", id);
        PaymentDto dto = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/payments/{id}/status")
    @Operation(summary = "Get Authoritative Payment Status")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentStatus(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/payments/{}/status]", id);
        PaymentDto dto = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/payments/my")
    @Operation(summary = "Get Current Resident Payment History")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getMyPayments(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [GET /api/v1/payments/my] -> Flat ID: {}", currentUser.getFlatId());
        List<PaymentDto> payments = paymentService.getMyPayments(currentUser.getFlatId());
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/payments/flat/{flatNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER') or hasRole('MAINTENANCE_FLAT') or hasRole('FLAT_MEMBER')")
    @Operation(summary = "Get Specific Flat Payment History")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPaymentsForFlat(@PathVariable String flatNumber) {
        log.info("REST REQUEST [GET /api/v1/payments/flat/{}]", flatNumber);
        List<PaymentDto> payments = paymentService.getPaymentsForFlatNumber(flatNumber);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/payments/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER')")
    @Operation(summary = "Get All Apartment Payments History Directory")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getAllPayments() {
        log.info("REST REQUEST [GET /api/v1/payments/all]");
        List<PaymentDto> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/payments/{id}/receipt")
    @Operation(summary = "Get Payment Receipt Details")
    public ResponseEntity<ApiResponse<ReceiptDto>> getReceipt(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/payments/{}/receipt]", id);
        ReceiptDto receipt = paymentService.getReceiptByPaymentId(id);
        return ResponseEntity.ok(ApiResponse.success(receipt));
    }

    @PostMapping("/payments/{id}/refund")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER')")
    @Operation(summary = "Refund Successful Payment")
    public ResponseEntity<ApiResponse<PaymentDto>> refundPayment(
            @PathVariable String id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/payments/{}/refund] -> Reason: {}", id, reason);
        User admin = userRepository.findById(currentUser.getId()).orElse(null);
        PaymentDto dto = paymentService.refundPayment(id, reason, admin);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", dto));
    }

    @PostMapping("/payments/record-cash")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TREASURER') or hasRole('MAINTENANCE_FLAT')")
    @Operation(summary = "Record Offline Cash Maintenance Payment")
    public ResponseEntity<ApiResponse<PaymentDto>> recordCashPayment(
            @Valid @RequestBody RecordCashPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/payments/record-cash] -> Flat: {}, Amount: {}", request.getFlatNumber(), request.getAmount());
        User recorder = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        PaymentDto dto = paymentService.recordCashPayment(request, recorder);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Offline cash payment recorded and receipt generated", dto));
    }
}
