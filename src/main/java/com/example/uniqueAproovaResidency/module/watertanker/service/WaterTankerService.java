package com.example.uniqueAproovaResidency.module.watertanker.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.watertanker.dto.CreateWaterTankerRequest;
import com.example.uniqueAproovaResidency.module.watertanker.dto.WaterTankerDto;
import com.example.uniqueAproovaResidency.module.watertanker.entity.WaterTanker;
import com.example.uniqueAproovaResidency.module.watertanker.repository.WaterTankerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterTankerService {

    private final WaterTankerRepository tankerRepository;

    @Transactional(readOnly = true)
    public List<WaterTankerDto> getAllTankers() {
        return tankerRepository.findAllByOrderByTankerDateDesc().stream()
                .map(WaterTankerDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WaterTankerDto getTankerById(String id) {
        WaterTanker tanker = tankerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WaterTanker", "id", id));
        return WaterTankerDto.fromEntity(tanker);
    }

    @Transactional
    public WaterTankerDto addTanker(CreateWaterTankerRequest request, User recorder) {
        WaterTanker tanker = WaterTanker.builder()
                .id(UUID.randomUUID().toString())
                .supplier(request.getSupplier())
                .tankerDate(request.getTankerDate() != null ? request.getTankerDate() : LocalDate.now())
                .quantityLiters(request.getQuantityLiters())
                .amount(request.getAmount())
                .reason(request.getReason())
                .invoiceDocumentId(request.getInvoiceDocumentId())
                .recordedBy(recorder)
                .approvalStatus("APPROVED")
                .paymentStatus("PAID")
                .build();

        WaterTanker saved = tankerRepository.save(tanker);
        return WaterTankerDto.fromEntity(saved);
    }
}
