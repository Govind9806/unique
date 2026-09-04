package com.example.uniqueAproovaResidency.module.notice.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.notice.dto.NoticeDto;
import com.example.uniqueAproovaResidency.module.notice.service.NoticeService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = "Notices & Announcements", description = "Endpoints for broadcasting apartment notices and announcements with priorities and pins")
public class NoticeController {

    private final NoticeService noticeService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Active & Pinned Notices")
    public ResponseEntity<ApiResponse<List<NoticeDto>>> getAllNotices() {
        log.info("REST REQUEST [GET /api/v1/notices]");
        List<NoticeDto> notices = noticeService.getAllNotices();
        log.info("REST RESPONSE [GET /api/v1/notices] -> Total notices found: {}", notices.size());
        return ResponseEntity.ok(ApiResponse.success(notices));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER') or hasRole('TREASURER')")
    @Operation(summary = "Publish New Notice")
    public ResponseEntity<ApiResponse<NoticeDto>> createNotice(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false, defaultValue = "NORMAL") String priority,
            @RequestParam(required = false, defaultValue = "false") Boolean isPinned,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/notices] -> Title: {}, Priority: {}, Pinned: {}", title, priority, isPinned);
        User creator = userRepository.findById(currentUser.getId()).orElse(null);
        NoticeDto dto = noticeService.createNotice(title, description, priority, isPinned, expiryDate, creator);
        log.info("REST RESPONSE [POST /api/v1/notices] -> Created Notice ID: {}", dto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notice published", dto));
    }
}
