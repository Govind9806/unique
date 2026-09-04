package com.example.uniqueAproovaResidency.module.water.repository;

import com.example.uniqueAproovaResidency.module.water.entity.WaterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterReadingRepository extends JpaRepository<WaterReading, String> {
    List<WaterReading> findByFlatIdOrderByReadingDateDesc(String flatId);
    Optional<WaterReading> findFirstByFlatIdOrderByReadingDateDesc(String flatId);
}
