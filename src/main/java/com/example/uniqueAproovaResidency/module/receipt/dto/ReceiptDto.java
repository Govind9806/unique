package com.example.uniqueAproovaResidency.module.receipt.dto;

import com.example.uniqueAproovaResidency.module.receipt.entity.Receipt;
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
public class ReceiptDto {
    private String id;
    private String receiptNumber;
    private String paymentId;
    private String flatId;
    private String flatNumber;
    private String residentName;
    private String billPeriod;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paidDate;
    private String apartmentName;
    private String pdfUrl;

    public static ReceiptDto fromEntity(Receipt receipt) {
        return ReceiptDto.builder()
                .id(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .paymentId(receipt.getPayment().getId())
                .flatId(receipt.getFlat().getId())
                .flatNumber(receipt.getFlat().getFlatNumber())
                .residentName(receipt.getResidentName())
                .billPeriod(receipt.getBillPeriod())
                .amount(receipt.getAmount())
                .paymentMethod(receipt.getPaymentMethod())
                .transactionId(receipt.getTransactionId())
                .paidDate(receipt.getPaidDate())
                .apartmentName("Aproova Residency")
                .pdfUrl(receipt.getPdfUrl())
                .build();
    }
}
