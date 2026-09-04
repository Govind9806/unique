package com.example.uniqueAproovaResidency.module.work.dto;

import com.example.uniqueAproovaResidency.module.work.entity.Work;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkDto {
    private String id;
    private String title;
    private String description;
    private String category;
    private String location;
    private String responsibleFlatNumber;
    private String vendorName;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String status;
    private LocalDate startDate;
    private LocalDate completionDate;
    private String createdByName;
    private String approvedByName;
    private String photoUrl;
    private java.util.List<String> photoUrls;
    private java.util.List<String> approvedFlatNumbers;
    private java.util.List<String> rejectedFlatNumbers;
    private java.util.Map<String, String> flatVoteReasons;
    private int acceptCount;
    private int rejectCount;
    private int pendingCount;
    private int majorityRequired;
    private int totalVotes;
    private String maintenanceDecision;
    private String maintenanceMessage;
    private LocalDateTime maintenanceRespondedAt;
    private LocalDateTime createdAt;

    public static WorkDto fromEntity(Work work) {
        return fromEntity(work, null);
    }

    public static WorkDto fromEntity(Work work, java.util.List<com.example.uniqueAproovaResidency.module.work.entity.WorkVote> votes) {
        java.util.List<String> app = new java.util.ArrayList<>();
        java.util.List<String> rej = new java.util.ArrayList<>();
        java.util.Map<String, String> voteReasons = new java.util.HashMap<>();

        if (votes != null && !votes.isEmpty()) {
            for (com.example.uniqueAproovaResidency.module.work.entity.WorkVote v : votes) {
                if ("REJECT".equalsIgnoreCase(v.getVote())) {
                    if (!rej.contains(v.getFlatNumber())) rej.add(v.getFlatNumber());
                } else {
                    if (!app.contains(v.getFlatNumber())) app.add(v.getFlatNumber());
                }
                if (v.getReason() != null && !v.getReason().isBlank()) {
                    voteReasons.put(v.getFlatNumber(), v.getReason().trim());
                }
            }
        } else {
            if (work.getApprovedFlatNumbers() != null) app.addAll(work.getApprovedFlatNumbers());
            if (work.getRejectedFlatNumbers() != null) rej.addAll(work.getRejectedFlatNumbers());
            if (work.getFlatVoteReasons() != null) voteReasons.putAll(work.getFlatVoteReasons());
        }

        java.util.List<String> photos = work.getPhotoUrls() != null ? new java.util.ArrayList<>(work.getPhotoUrls()) : new java.util.ArrayList<>();
        if (photos.isEmpty() && work.getPhotoUrl() != null && !work.getPhotoUrl().isBlank()) {
            photos.add(work.getPhotoUrl());
        }

        String flatNum = null;
        try {
            if (work.getResponsibleFlat() != null) flatNum = work.getResponsibleFlat().getFlatNumber();
        } catch (Exception ignored) {}

        String cName = null;
        try {
            if (work.getCreatedBy() != null) cName = work.getCreatedBy().getName();
        } catch (Exception ignored) {}

        String aName = null;
        try {
            if (work.getApprovedBy() != null) aName = work.getApprovedBy().getName();
        } catch (Exception ignored) {}

        int totalFlats = 16;
        int accepts = app.size();
        int rejects = rej.size();
        int pending = Math.max(0, totalFlats - (accepts + rejects));

        return WorkDto.builder()
                .id(work.getId())
                .title(work.getTitle())
                .description(work.getDescription())
                .category(work.getCategory())
                .location(work.getLocation())
                .responsibleFlatNumber(flatNum)
                .vendorName(work.getVendorName())
                .estimatedCost(work.getEstimatedCost())
                .actualCost(work.getActualCost())
                .status(work.getStatus())
                .startDate(work.getStartDate())
                .completionDate(work.getCompletionDate())
                .createdByName(cName)
                .approvedByName(aName)
                .photoUrl(work.getPhotoUrl())
                .photoUrls(photos)
                .approvedFlatNumbers(app)
                .rejectedFlatNumbers(rej)
                .flatVoteReasons(voteReasons)
                .acceptCount(accepts)
                .rejectCount(rejects)
                .pendingCount(pending)
                .majorityRequired(9)
                .totalVotes(accepts + rejects)
                .maintenanceDecision(work.getMaintenanceDecision())
                .maintenanceMessage(work.getMaintenanceMessage())
                .maintenanceRespondedAt(work.getMaintenanceRespondedAt())
                .createdAt(work.getCreatedAt())
                .build();
    }
}
