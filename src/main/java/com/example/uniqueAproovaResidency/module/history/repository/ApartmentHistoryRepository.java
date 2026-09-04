package com.example.uniqueAproovaResidency.module.history.repository;

import com.example.uniqueAproovaResidency.module.history.entity.ApartmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentHistoryRepository extends JpaRepository<ApartmentHistory, String> {
    List<ApartmentHistory> findAllByOrderByCreatedAtDesc();
    List<ApartmentHistory> findByEventCategoryOrderByCreatedAtDesc(String eventCategory);
}
