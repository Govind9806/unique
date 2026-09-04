package com.example.uniqueAproovaResidency.module.appversion.entity;

import com.example.uniqueAproovaResidency.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "app_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersion extends BaseEntity {

    @Id
    private String id;

    @Column(name = "latest_version", nullable = false)
    private String latestVersion;

    @Column(name = "minimum_supported_version", nullable = false)
    private String minimumSupportedVersion;

    @Column(name = "apk_url", nullable = false)
    private String apkUrl;

    @Column(name = "release_notes")
    private String releaseNotes;

    @Column(name = "force_update", nullable = false)
    @Builder.Default
    private Boolean forceUpdate = false;
}
