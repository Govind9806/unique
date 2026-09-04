package com.example.uniqueAproovaResidency.module.apartment.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.apartment.entity.Apartment;
import com.example.uniqueAproovaResidency.module.apartment.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    @Transactional(readOnly = true)
    public Apartment getApartmentInfo() {
        return apartmentRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Apartment", "config", "default"));
    }

    @Transactional
    public Apartment updateApartmentInfo(Apartment updated) {
        Apartment existing = getApartmentInfo();
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setTotalFlats(updated.getTotalFlats());
        existing.setCurrency(updated.getCurrency());
        existing.setTimezone(updated.getTimezone());
        return apartmentRepository.save(existing);
    }
}
