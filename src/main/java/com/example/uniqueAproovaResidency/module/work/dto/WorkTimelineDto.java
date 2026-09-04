package com.example.uniqueAproovaResidency.module.work.dto;

import com.example.uniqueAproovaResidency.module.work.entity.WorkTimeline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkTimelineDto {
    private String id;
    private String status;
    private String description;
    private String createdByName;
    private LocalDateTime createdAt;

    public static WorkTimelineDto fromEntity(WorkTimeline timeline) {
        return WorkTimelineDto.builder()
                .id(timeline.getId())
                .status(timeline.getStatus())
                .description(timeline.getDescription())
                .createdByName(timeline.getCreatedBy() != null ? timeline.getCreatedBy().getName() : null)
                .createdAt(timeline.getCreatedAt())
                .build();
    }
}
