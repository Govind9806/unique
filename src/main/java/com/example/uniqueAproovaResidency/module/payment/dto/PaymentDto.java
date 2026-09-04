package com.example.uniqueAproovaResidency.module.payment.dto;

import com.example.uniqueAproovaResidency.module.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private String id;
    private String billId;
    private String flatId;
    private String flatNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String provider;
    private String merchantReference;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String transactionReference;
    private String status;
    private String failureReason;
    private String upiUri;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static PaymentDto fromEntity(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .billId(payment.getBill() != null ? payment.getBill().getId() : null)
                .flatId(payment.getFlat().getId())
                .flatNumber(payment.getFlat().getFlatNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .provider(payment.getProvider())
                .merchantReference(payment.getGatewayOrderId())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .transactionReference(payment.getTransactionReference())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
