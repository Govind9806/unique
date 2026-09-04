package com.example.uniqueAproovaResidency.module.responsibility.dto;

import com.example.uniqueAproovaResidency.module.responsibility.entity.Responsibility;
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
public class ResponsibilityDto {
    private String id;
    private String responsibilityType;
    private String flatId;
    private String flatNumber;
    private String assignedUserId;
    private String assignedUserName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String assignedByName;
    private LocalDateTime createdAt;

    public static ResponsibilityDto fromEntity(Responsibility entity) {
        return ResponsibilityDto.builder()
                .id(entity.getId())
                .responsibilityType(entity.getResponsibilityType())
                .flatId(entity.getFlat() != null ? entity.getFlat().getId() : null)
                .flatNumber(entity.getFlat() != null ? entity.getFlat().getFlatNumber() : null)
                .assignedUserId(entity.getAssignedUser() != null ? entity.getAssignedUser().getId() : null)
                .assignedUserName(entity.getAssignedUser() != null ? entity.getAssignedUser().getName() : null)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .assignedByName(entity.getAssignedBy() != null ? entity.getAssignedBy().getName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
