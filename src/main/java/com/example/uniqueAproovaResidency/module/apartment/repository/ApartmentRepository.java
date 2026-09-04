package com.example.uniqueAproovaResidency.module.apartment.repository;

import com.example.uniqueAproovaResidency.module.apartment.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, String> {
}
