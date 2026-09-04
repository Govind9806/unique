package com.example.uniqueAproovaResidency.module.payment.repository;

import com.example.uniqueAproovaResidency.module.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByFlatId(String flatId);
    List<Payment> findByFlatIdOrderByPaidAtDesc(String flatId);
    List<Payment> findByFlatFlatNumberOrderByPaidAtDesc(String flatNumber);
    List<Payment> findAllByOrderByPaidAtDesc();
    List<Payment> findByBillId(String billId);
    List<Payment> findByStatus(String status);
    List<Payment> findByStatusIn(List<String> statuses);
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
    Optional<Payment> findByTransactionReference(String transactionReference);
    boolean existsByGatewayPaymentIdAndStatus(String gatewayPaymentId, String status);
}
