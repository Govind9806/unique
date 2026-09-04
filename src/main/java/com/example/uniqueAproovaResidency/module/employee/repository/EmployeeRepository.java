package com.example.uniqueAproovaResidency.module.employee.repository;

import com.example.uniqueAproovaResidency.module.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByStatus(String status);
}
