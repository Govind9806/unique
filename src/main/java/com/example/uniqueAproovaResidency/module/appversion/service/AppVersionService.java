package com.example.uniqueAproovaResidency.module.appversion.service;

import com.example.uniqueAproovaResidency.module.appversion.entity.AppVersion;
import com.example.uniqueAproovaResidency.module.appversion.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionRepository appVersionRepository;

    @Transactional(readOnly = true)
    public AppVersion getLatestAppVersion() {
        return appVersionRepository.findAll().stream().findFirst()
                .orElseGet(() -> AppVersion.builder()
                        .id(UUID.randomUUID().toString())
                        .latestVersion("1.0.0")
                        .minimumSupportedVersion("1.0.0")
                        .apkUrl("https://aproova.app/downloads/aproova-residency-v1.0.0.apk")
                        .releaseNotes("Initial release of Aproova Residency APK.")
                        .forceUpdate(false)
                        .build());
    }
}
