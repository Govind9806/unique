package com.example.uniqueAproovaResidency.module.notification.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.notification.dto.NotificationDto;
import com.example.uniqueAproovaResidency.module.notification.service.NotificationService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications & Reminders", description = "Endpoints for retrieving and managing in-app notifications and payment reminders")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get Current User Notifications")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [GET /api/v1/notifications] -> User ID: {}", currentUser.getId());
        List<NotificationDto> notifications = notificationService.getUserNotifications(currentUser.getId());
        log.info("REST RESPONSE [GET /api/v1/notifications] -> Total notifications: {}", notifications.size());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark Notification as Read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable String id, @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/notifications/{}/read] -> User ID: {}", id, currentUser.getId());
        notificationService.markAsRead(id, currentUser.getId());
        log.info("REST RESPONSE [POST /api/v1/notifications/{}/read] -> Marked read successfully", id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", "Read"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark All Notifications as Read")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/notifications/read-all] -> User ID: {}", currentUser.getId());
        notificationService.markAllAsRead(currentUser.getId());
        log.info("REST RESPONSE [POST /api/v1/notifications/read-all] -> Marked all read successfully");
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", "Read All"));
    }
}
