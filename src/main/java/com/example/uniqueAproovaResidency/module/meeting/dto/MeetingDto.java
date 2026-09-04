package com.example.uniqueAproovaResidency.module.meeting.dto;

import com.example.uniqueAproovaResidency.module.meeting.entity.Meeting;
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
public class MeetingDto {
    private String id;
    private String title;
    private LocalDate meetingDate;
    private String meetingTime;
    private String location;
    private String agenda;
    private String minutesDecisions;
    private String minutesNotes;
    private String status;
    private String createdByName;
    private LocalDateTime createdAt;

    public static MeetingDto fromEntity(Meeting meeting) {
        return MeetingDto.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .meetingDate(meeting.getMeetingDate())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .agenda(meeting.getAgenda())
                .minutesDecisions(meeting.getMinutesDecisions())
                .minutesNotes(meeting.getMinutesNotes())
                .status(meeting.getStatus())
                .createdByName(meeting.getCreatedBy() != null ? meeting.getCreatedBy().getName() : null)
                .createdAt(meeting.getCreatedAt())
                .build();
    }
}
