package com.example.uniqueAproovaResidency.module.work.repository;

import com.example.uniqueAproovaResidency.module.work.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkRepository extends JpaRepository<Work, String> {
    List<Work> findAllByOrderByCreatedAtDesc();
    List<Work> findByStatus(String status);
}
