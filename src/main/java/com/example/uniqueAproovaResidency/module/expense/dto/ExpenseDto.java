package com.example.uniqueAproovaResidency.module.expense.dto;

import com.example.uniqueAproovaResidency.module.expense.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto {
    private String id;
    private String category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String vendor;
    private String relatedWorkId;
    private String createdByName;
    private String responsibleFlatNumber;
    private String approvalStatus;
    private String paymentStatus;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private List<String> approvedFlatNumbers;
    private List<String> rejectedFlatNumbers;
    private Integer approvedCount;
    private Integer totalRequiredFlats;
    private Boolean isEmergency;
    private String photoUrl;
    private String receiptPhotoDocumentId;

    public static ExpenseDto fromEntity(Expense expense) {
        List<String> appFlats = expense.getApprovedFlatNumbers() != null ? expense.getApprovedFlatNumbers() : java.util.List.of();
        List<String> rejFlats = expense.getRejectedFlatNumbers() != null ? expense.getRejectedFlatNumbers() : java.util.List.of();

        String photo = expense.getPhotoUrl();
        if (photo == null || photo.isBlank()) {
            if (expense.getReceiptPhotoDocumentId() != null && !expense.getReceiptPhotoDocumentId().isBlank()) {
                photo = "/api/v1/documents/" + expense.getReceiptPhotoDocumentId() + "/file";
            }
        }

        return ExpenseDto.builder()
                .id(expense.getId())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .vendor(expense.getVendor())
                .relatedWorkId(expense.getRelatedWork() != null ? expense.getRelatedWork().getId() : null)
                .createdByName(expense.getCreatedBy() != null ? expense.getCreatedBy().getName() : null)
                .responsibleFlatNumber(expense.getResponsibleFlat() != null ? expense.getResponsibleFlat().getFlatNumber() : null)
                .approvalStatus(expense.getApprovalStatus())
                .paymentStatus(expense.getPaymentStatus())
                .approvedByName(expense.getApprovedBy() != null ? expense.getApprovedBy().getName() : null)
                .approvedAt(expense.getApprovedAt())
                .rejectionReason(expense.getRejectionReason())
                .createdAt(expense.getCreatedAt())
                .approvedFlatNumbers(appFlats)
                .rejectedFlatNumbers(rejFlats)
                .approvedCount(appFlats.size())
                .totalRequiredFlats(16)
                .isEmergency(expense.getIsEmergency() != null ? expense.getIsEmergency() : false)
                .photoUrl(photo)
                .receiptPhotoDocumentId(expense.getReceiptPhotoDocumentId())
                .build();
    }
}
