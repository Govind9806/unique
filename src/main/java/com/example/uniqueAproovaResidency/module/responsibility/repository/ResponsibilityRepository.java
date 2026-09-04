package com.example.uniqueAproovaResidency.module.responsibility.repository;

import com.example.uniqueAproovaResidency.module.responsibility.entity.Responsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponsibilityRepository extends JpaRepository<Responsibility, String> {
    List<Responsibility> findByEndDateIsNull();
    Optional<Responsibility> findByResponsibilityTypeAndEndDateIsNull(String responsibilityType);
    List<Responsibility> findByResponsibilityTypeOrderByCreatedAtDesc(String responsibilityType);
    List<Responsibility> findAllByOrderByCreatedAtDesc();
}
