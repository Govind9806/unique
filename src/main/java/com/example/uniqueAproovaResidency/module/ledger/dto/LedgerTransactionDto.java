package com.example.uniqueAproovaResidency.module.ledger.dto;

import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
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
public class LedgerTransactionDto {
    private String id;
    private String type; // INCOME, EXPENSE, ADJUSTMENT
    private String category;
    private BigDecimal amount;
    private String description;
    private String referenceType;
    private String referenceId;
    private LocalDateTime transactionDate;
    private String createdByName;
    private String photoUrl;
    private String receiptPhotoDocumentId;
    private String vendor;

    public static LedgerTransactionDto fromEntity(LedgerTransaction tx) {
        String pUrl = tx.getPhotoUrl();
        String docId = tx.getReceiptPhotoDocumentId();
        if ((pUrl == null || pUrl.isEmpty()) && docId != null && !docId.isEmpty()) {
            pUrl = "/api/v1/documents/" + docId + "/file";
        }
        return LedgerTransactionDto.builder()
                .id(tx.getId())
                .type(tx.getType())
                .category(tx.getCategory())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .referenceType(tx.getReferenceType())
                .referenceId(tx.getReferenceId())
                .transactionDate(tx.getTransactionDate())
                .createdByName(tx.getCreatedBy() != null ? tx.getCreatedBy().getName() : null)
                .photoUrl(pUrl)
                .receiptPhotoDocumentId(docId)
                .vendor(tx.getVendor())
                .build();
    }
}
