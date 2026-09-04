package com.example.uniqueAproovaResidency.module.notification.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.notification.dto.NotificationDto;
import com.example.uniqueAproovaResidency.module.notification.entity.Notification;
import com.example.uniqueAproovaResidency.module.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(String userId) {
        log.info("SERVICE [NotificationService.getUserNotifications] -> User ID: {}", userId);
        List<NotificationDto> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
        log.info("SERVICE [NotificationService.getUserNotifications] -> Found {} notifications for User ID: {}", notifications.size(), userId);
        return notifications;
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        log.info("SERVICE [NotificationService.getUnreadCount] -> User ID: {}", userId);
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        log.info("SERVICE [NotificationService.getUnreadCount] -> Unread count: {}", count);
        return count;
    }

    @Transactional
    public void markAsRead(String id, String userId) {
        log.info("SERVICE [NotificationService.markAsRead] -> Notification ID: {}, User ID: {}", id, userId);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (notification.getUser().getId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
            log.info("SERVICE [NotificationService.markAsRead] -> Saved isRead=true for Notification ID: {}", id);
        } else {
            log.warn("SERVICE [NotificationService.markAsRead] -> User ID {} mismatch for Notification ID {}", userId, id);
        }
    }

    @Transactional
    public void markAllAsRead(String userId) {
        log.info("SERVICE [NotificationService.markAllAsRead] -> User ID: {}", userId);
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(list);
        log.info("SERVICE [NotificationService.markAllAsRead] -> Marked {} notifications as read for User ID: {}", list.size(), userId);
    }
}
