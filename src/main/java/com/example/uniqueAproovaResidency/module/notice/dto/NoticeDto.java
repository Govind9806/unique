package com.example.uniqueAproovaResidency.module.notice.dto;

import com.example.uniqueAproovaResidency.module.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {
    private String id;
    private String title;
    private String description;
    private String priority;
    private Boolean isPinned;
    private LocalDate expiryDate;
    private String createdByName;
    private LocalDateTime createdAt;

    public static NoticeDto fromEntity(Notice notice) {
        return NoticeDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .description(notice.getDescription())
                .priority(notice.getPriority())
                .isPinned(notice.getIsPinned())
                .expiryDate(notice.getExpiryDate())
                .createdByName(notice.getCreatedBy() != null ? notice.getCreatedBy().getName() : null)
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
