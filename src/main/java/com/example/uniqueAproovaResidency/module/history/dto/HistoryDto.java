package com.example.uniqueAproovaResidency.module.history.dto;

import com.example.uniqueAproovaResidency.module.history.entity.ApartmentHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDto {
    private String id;
    private String eventCategory;
    private String title;
    private String description;
    private String referenceType;
    private String referenceId;
    private String createdByName;
    private LocalDateTime createdAt;

    public static HistoryDto fromEntity(ApartmentHistory history) {
        return HistoryDto.builder()
                .id(history.getId())
                .eventCategory(history.getEventCategory())
                .title(history.getTitle())
                .description(history.getDescription())
                .referenceType(history.getReferenceType())
                .referenceId(history.getReferenceId())
                .createdByName(history.getCreatedBy() != null ? history.getCreatedBy().getName() : null)
                .createdAt(history.getCreatedAt())
                .build();
    }
}
