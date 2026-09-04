package com.example.uniqueAproovaResidency.module.settings.repository;

import com.example.uniqueAproovaResidency.module.settings.entity.ApartmentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentSettingsRepository extends JpaRepository<ApartmentSettings, String> {
}
