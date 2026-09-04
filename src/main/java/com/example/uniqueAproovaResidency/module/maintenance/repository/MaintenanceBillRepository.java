package com.example.uniqueAproovaResidency.module.maintenance.repository;

import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceBillRepository extends JpaRepository<MaintenanceBill, String> {
    List<MaintenanceBill> findByFlatId(String flatId);
    List<MaintenanceBill> findByFlatIdAndStatus(String flatId, String status);
    List<MaintenanceBill> findByBillingMonthAndBillingYear(Integer month, Integer year);
    Optional<MaintenanceBill> findByFlatIdAndBillingMonthAndBillingYear(String flatId, Integer month, Integer year);
    boolean existsByFlatIdAndBillingMonthAndBillingYear(String flatId, Integer month, Integer year);
}
