package com.example.uniqueAproovaResidency.module.document.dto;

import com.example.uniqueAproovaResidency.module.document.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private String id;
    private String fileName;
    private String fileType;
    private String filePath;
    private String documentType;
    private String relatedEntityType;
    private String relatedEntityId;
    private String uploadedByName;
    private LocalDateTime createdAt;

    public static DocumentDto fromEntity(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .filePath(document.getFilePath())
                .documentType(document.getDocumentType())
                .relatedEntityType(document.getRelatedEntityType())
                .relatedEntityId(document.getRelatedEntityId())
                .uploadedByName(document.getUploadedBy() != null ? document.getUploadedBy().getName() : null)
                .createdAt(document.getCreatedAt())
                .build();
    }
}
