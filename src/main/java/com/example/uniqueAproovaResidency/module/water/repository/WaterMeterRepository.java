package com.example.uniqueAproovaResidency.module.water.repository;

import com.example.uniqueAproovaResidency.module.water.entity.WaterMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaterMeterRepository extends JpaRepository<WaterMeter, String> {
    Optional<WaterMeter> findByFlatId(String flatId);
    Optional<WaterMeter> findByMeterNumber(String meterNumber);
}
