package com.example.uniqueAproovaResidency.module.notice.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.notice.dto.NoticeDto;
import com.example.uniqueAproovaResidency.module.notice.entity.Notice;
import com.example.uniqueAproovaResidency.module.notice.repository.NoticeRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public List<NoticeDto> getAllNotices() {
        log.info("SERVICE [NoticeService.getAllNotices]");
        List<NoticeDto> notices = noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc().stream()
                .map(NoticeDto::fromEntity)
                .collect(Collectors.toList());
        log.info("SERVICE [NoticeService.getAllNotices] -> Total notices found: {}", notices.size());
        return notices;
    }

    @Transactional
    public NoticeDto createNotice(String title, String description, String priority, Boolean isPinned, LocalDate expiryDate, User creator) {
        log.info("SERVICE [NoticeService.createNotice] -> Title: {}, Priority: {}, Pinned: {}, Creator: {}",
                title, priority, isPinned, creator != null ? creator.getName() : "System");

        Notice notice = Notice.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .priority(priority != null ? priority.toUpperCase() : "NORMAL")
                .isPinned(isPinned != null ? isPinned : false)
                .expiryDate(expiryDate)
                .createdBy(creator)
                .build();

        Notice saved = noticeRepository.save(notice);
        log.info("SERVICE [NoticeService.createNotice] -> Notice published with ID: {}", saved.getId());
        return NoticeDto.fromEntity(saved);
    }
}
