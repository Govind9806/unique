package com.example.uniqueAproovaResidency.module.flat.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.flat.dto.FlatDto;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.user.dto.UserDto;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlatService {

    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FlatDto> getAllFlats() {
        return flatRepository.findAll().stream()
                .map(flat -> {
                    List<UserDto> members = userRepository.findByFlatId(flat.getId()).stream()
                            .map(UserDto::fromEntity)
                            .collect(Collectors.toList());
                    return FlatDto.fromEntity(flat, members);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FlatDto getFlatById(String id) {
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", id));
        List<UserDto> members = userRepository.findByFlatId(flat.getId()).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return FlatDto.fromEntity(flat, members);
    }

    @Transactional
    public FlatDto createFlat(String flatNumber, Integer floor) {
        if (flatRepository.existsByFlatNumber(flatNumber)) {
            throw new BusinessRuleException("FLAT_EXISTS", "Flat already exists: " + flatNumber);
        }

        Flat flat = Flat.builder()
                .id(UUID.randomUUID().toString())
                .flatNumber(flatNumber)
                .floor(floor)
                .status("OCCUPIED")
                .build();

        Flat saved = flatRepository.save(flat);
        return FlatDto.fromEntity(saved, List.of());
    }

    @Transactional
    public FlatDto updateFlat(String id, String flatNumber, Integer floor, String status) {
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", id));

        flat.setFlatNumber(flatNumber);
        flat.setFloor(floor);
        flat.setStatus(status);

        Flat saved = flatRepository.save(flat);
        List<UserDto> members = userRepository.findByFlatId(flat.getId()).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return FlatDto.fromEntity(saved, members);
    }
}
