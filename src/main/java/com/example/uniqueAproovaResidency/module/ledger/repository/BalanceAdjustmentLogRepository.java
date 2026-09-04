package com.example.uniqueAproovaResidency.module.ledger.repository;

import com.example.uniqueAproovaResidency.module.ledger.entity.BalanceAdjustmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceAdjustmentLogRepository extends JpaRepository<BalanceAdjustmentLog, String> {
    List<BalanceAdjustmentLog> findAllByOrderByModifiedAtDesc();
}
