package com.example.uniqueAproovaResidency.module.watertanker.repository;

import com.example.uniqueAproovaResidency.module.watertanker.entity.WaterTanker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterTankerRepository extends JpaRepository<WaterTanker, String> {
    List<WaterTanker> findAllByOrderByTankerDateDesc();
}
