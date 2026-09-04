package com.example.uniqueAproovaResidency.module.appversion.repository;

import com.example.uniqueAproovaResidency.module.appversion.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, String> {
}
