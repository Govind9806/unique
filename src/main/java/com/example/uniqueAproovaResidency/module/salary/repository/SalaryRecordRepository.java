package com.example.uniqueAproovaResidency.module.salary.repository;

import com.example.uniqueAproovaResidency.module.salary.entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, String> {
    List<SalaryRecord> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    List<SalaryRecord> findAllByOrderByCreatedAtDesc();
}
