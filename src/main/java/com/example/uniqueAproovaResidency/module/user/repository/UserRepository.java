package com.example.uniqueAproovaResidency.module.user.repository;

import com.example.uniqueAproovaResidency.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:identifier) OR u.phone = :identifier OR u.phone = CONCAT('+91', :identifier) OR REPLACE(u.phone, '+91', '') = :identifier")
    Optional<User> findByEmailOrPhone(@Param("identifier") String identifier);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByFlatId(String flatId);
}
