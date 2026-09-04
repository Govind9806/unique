package com.example.uniqueAproovaResidency.module.ledger.repository;

import com.example.uniqueAproovaResidency.module.ledger.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerTransaction, String> {

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerTransaction l WHERE l.type = 'INCOME'")
    BigDecimal getTotalIncome();

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerTransaction l WHERE l.type = 'EXPENSE'")
    BigDecimal getTotalExpense();

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerTransaction l WHERE l.type = 'INCOME' AND l.transactionDate >= :startDate AND l.transactionDate <= :endDate")
    BigDecimal getMonthlyIncome(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerTransaction l WHERE l.type = 'EXPENSE' AND l.transactionDate >= :startDate AND l.transactionDate <= :endDate")
    BigDecimal getMonthlyExpense(LocalDateTime startDate, LocalDateTime endDate);

    List<LedgerTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    List<LedgerTransaction> findAllByOrderByTransactionDateDesc();

    boolean existsByReferenceTypeAndReferenceId(String referenceType, String referenceId);
}
