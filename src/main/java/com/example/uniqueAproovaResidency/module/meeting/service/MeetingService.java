package com.example.uniqueAproovaResidency.module.meeting.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.meeting.dto.MeetingDto;
import com.example.uniqueAproovaResidency.module.meeting.entity.Meeting;
import com.example.uniqueAproovaResidency.module.meeting.repository.MeetingRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    @Transactional(readOnly = true)
    public List<MeetingDto> getAllMeetings() {
        return meetingRepository.findAllByOrderByMeetingDateDesc().stream()
                .map(MeetingDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeetingDto getMeetingById(String id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", id));
        return MeetingDto.fromEntity(meeting);
    }

    @Transactional
    public MeetingDto createMeeting(String title, LocalDate date, String time, String location, String agenda, User creator) {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .meetingDate(date)
                .meetingTime(time)
                .location(location)
                .agenda(agenda)
                .status("SCHEDULED")
                .createdBy(creator)
                .build();

        Meeting saved = meetingRepository.save(meeting);
        return MeetingDto.fromEntity(saved);
    }

    @Transactional
    public MeetingDto recordMinutes(String id, String decisions, String notes) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting", "id", id));

        meeting.setMinutesDecisions(decisions);
        meeting.setMinutesNotes(notes);
        meeting.setStatus("COMPLETED");

        Meeting saved = meetingRepository.save(meeting);
        return MeetingDto.fromEntity(saved);
    }
}
