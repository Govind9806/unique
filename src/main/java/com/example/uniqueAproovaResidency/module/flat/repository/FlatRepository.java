package com.example.uniqueAproovaResidency.module.flat.repository;

import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, String> {
    Optional<Flat> findByFlatNumber(String flatNumber);
    boolean existsByFlatNumber(String flatNumber);
}
