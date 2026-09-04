package com.example.uniqueAproovaResidency.module.meeting.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.meeting.dto.MeetingDto;
import com.example.uniqueAproovaResidency.module.meeting.service.MeetingService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Meetings Management", description = "Endpoints for scheduling society meetings and recording meeting minutes & decisions")
public class MeetingController {

    private final MeetingService meetingService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Society Meetings")
    public ResponseEntity<ApiResponse<List<MeetingDto>>> getAllMeetings() {
        List<MeetingDto> meetings = meetingService.getAllMeetings();
        return ResponseEntity.ok(ApiResponse.success(meetings));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Meeting Details by ID")
    public ResponseEntity<ApiResponse<MeetingDto>> getMeetingById(@PathVariable String id) {
        MeetingDto dto = meetingService.getMeetingById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_MEMBER') or hasRole('TREASURER')")
    @Operation(summary = "Schedule New Society Meeting")
    public ResponseEntity<ApiResponse<MeetingDto>> createMeeting(
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String time,
            @RequestParam String location,
            @RequestParam(required = false) String agenda,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User creator = userRepository.findById(currentUser.getId()).orElse(null);
        MeetingDto dto = meetingService.createMeeting(title, date, time, location, agenda, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Meeting scheduled", dto));
    }

    @PostMapping("/{id}/minutes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_MEMBER') or hasRole('TREASURER')")
    @Operation(summary = "Record Meeting Minutes and Decisions")
    public ResponseEntity<ApiResponse<MeetingDto>> recordMinutes(
            @PathVariable String id,
            @RequestParam String decisions,
            @RequestParam(required = false) String notes) {
        MeetingDto dto = meetingService.recordMinutes(id, decisions, notes);
        return ResponseEntity.ok(ApiResponse.success("Meeting minutes recorded", dto));
    }
}
