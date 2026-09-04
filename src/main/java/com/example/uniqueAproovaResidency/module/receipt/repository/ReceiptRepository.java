package com.example.uniqueAproovaResidency.module.receipt.repository;

import com.example.uniqueAproovaResidency.module.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String> {
    Optional<Receipt> findByPaymentId(String paymentId);
}
