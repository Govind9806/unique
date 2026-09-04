package com.example.uniqueAproovaResidency.module.electricity.repository;

import com.example.uniqueAproovaResidency.module.electricity.entity.ElectricityBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectricityBillRepository extends JpaRepository<ElectricityBill, String> {
    List<ElectricityBill> findAllByOrderByCreatedAtDesc();
}
