package com.example.uniqueAproovaResidency.module.settings.service;

import com.example.uniqueAproovaResidency.module.settings.entity.ApartmentSettings;
import com.example.uniqueAproovaResidency.module.settings.repository.ApartmentSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final ApartmentSettingsRepository settingsRepository;

    @Transactional(readOnly = true)
    public ApartmentSettings getSettings() {
        return settingsRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    ApartmentSettings defaults = ApartmentSettings.builder()
                            .id(UUID.randomUUID().toString())
                            .maintenanceAmount(BigDecimal.valueOf(2000))
                            .waterRatePerUnit(BigDecimal.valueOf(40))
                            .dueDayOfMonth(10)
                            .currency("INR")
                            .timezone("Asia/Kolkata")
                            .notificationEnabled(true)
                            .build();
                    return settingsRepository.save(defaults);
                });
    }

    @Transactional
    public ApartmentSettings updateSettings(ApartmentSettings updated) {
        ApartmentSettings current = getSettings();
        current.setMaintenanceAmount(updated.getMaintenanceAmount());
        current.setWaterRatePerUnit(updated.getWaterRatePerUnit());
        current.setDueDayOfMonth(updated.getDueDayOfMonth());
        current.setCurrency(updated.getCurrency());
        current.setTimezone(updated.getTimezone());
        current.setNotificationEnabled(updated.getNotificationEnabled());
        return settingsRepository.save(current);
    }
}
