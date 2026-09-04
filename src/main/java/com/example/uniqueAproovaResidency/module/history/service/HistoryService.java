package com.example.uniqueAproovaResidency.module.history.service;

import com.example.uniqueAproovaResidency.module.history.dto.HistoryDto;
import com.example.uniqueAproovaResidency.module.history.repository.ApartmentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final ApartmentHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public List<HistoryDto> getHistory(String category) {
        if (StringUtils.hasText(category) && !"ALL".equalsIgnoreCase(category)) {
            return historyRepository.findByEventCategoryOrderByCreatedAtDesc(category.toUpperCase()).stream()
                    .map(HistoryDto::fromEntity)
                    .collect(Collectors.toList());
        }
        return historyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(HistoryDto::fromEntity)
                .collect(Collectors.toList());
    }
}
