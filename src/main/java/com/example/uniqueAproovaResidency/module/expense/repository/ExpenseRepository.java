package com.example.uniqueAproovaResidency.module.expense.repository;

import com.example.uniqueAproovaResidency.module.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findAllByOrderByCreatedAtDesc();
    List<Expense> findByApprovalStatus(String approvalStatus);
}
