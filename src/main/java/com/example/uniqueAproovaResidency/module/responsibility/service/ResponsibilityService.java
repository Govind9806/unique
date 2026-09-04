package com.example.uniqueAproovaResidency.module.responsibility.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.responsibility.dto.CreateResponsibilityRequest;
import com.example.uniqueAproovaResidency.module.responsibility.dto.ResponsibilityDto;
import com.example.uniqueAproovaResidency.module.responsibility.entity.Responsibility;
import com.example.uniqueAproovaResidency.module.responsibility.repository.ResponsibilityRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResponsibilityService {

    private final ResponsibilityRepository responsibilityRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ResponsibilityDto> getCurrentResponsibilities() {
        return responsibilityRepository.findByEndDateIsNull().stream()
                .map(ResponsibilityDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResponsibilityDto getActiveResponsibilityByType(String type) {
        Responsibility resp = responsibilityRepository.findByResponsibilityTypeAndEndDateIsNull(type.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Responsibility", "type", type));
        return ResponsibilityDto.fromEntity(resp);
    }

    @Transactional
    public ResponsibilityDto assignResponsibility(CreateResponsibilityRequest request, String assignedByUserId) {
        String type = request.getResponsibilityType().toUpperCase();

        // 1. Close current active assignment for this responsibility type
        Optional<Responsibility> currentActive = responsibilityRepository.findByResponsibilityTypeAndEndDateIsNull(type);
        if (currentActive.isPresent()) {
            Responsibility active = currentActive.get();
            active.setEndDate(LocalDate.now());
            responsibilityRepository.save(active);
        }

        // 2. Fetch flat & assigned user
        Flat flat = flatRepository.findById(request.getFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", request.getFlatId()));

        User assignedUser = null;
        if (StringUtils.hasText(request.getAssignedUserId())) {
            assignedUser = userRepository.findById(request.getAssignedUserId()).orElse(null);
        }

        User assignedBy = userRepository.findById(assignedByUserId).orElse(null);

        // 3. Create new assignment
        Responsibility newResp = Responsibility.builder()
                .id(UUID.randomUUID().toString())
                .responsibilityType(type)
                .flat(flat)
                .assignedUser(assignedUser)
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .assignedBy(assignedBy)
                .build();

        Responsibility saved = responsibilityRepository.save(newResp);
        return ResponsibilityDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ResponsibilityDto> getResponsibilityHistory() {
        return responsibilityRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ResponsibilityDto::fromEntity)
                .collect(Collectors.toList());
    }
}
