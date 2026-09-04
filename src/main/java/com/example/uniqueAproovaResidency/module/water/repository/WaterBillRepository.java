package com.example.uniqueAproovaResidency.module.water.repository;

import com.example.uniqueAproovaResidency.module.water.entity.WaterBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterBillRepository extends JpaRepository<WaterBill, String> {
    List<WaterBill> findByFlatIdOrderByCreatedAtDesc(String flatId);
    Optional<WaterBill> findByReadingId(String readingId);
    Optional<WaterBill> findByFlatIdAndBillingPeriod(String flatId, String billingPeriod);
    boolean existsByFlatIdAndBillingPeriod(String flatId, String billingPeriod);
}
