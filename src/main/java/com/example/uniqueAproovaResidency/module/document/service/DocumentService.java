package com.example.uniqueAproovaResidency.module.document.service;

import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.document.dto.DocumentDto;
import com.example.uniqueAproovaResidency.module.document.entity.Document;
import com.example.uniqueAproovaResidency.module.document.repository.DocumentRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, String documentType, String relatedEntityType, String relatedEntityId, User uploader) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Document doc = Document.builder()
                .id(UUID.randomUUID().toString())
                .fileName(originalFilename)
                .fileType(file.getContentType() != null ? file.getContentType() : "image/jpeg")
                .filePath(filePath.toString())
                .documentType(documentType != null ? documentType.toUpperCase() : "OTHER")
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .uploadedBy(uploader)
                .build();

        Document saved = documentRepository.save(doc);
        return DocumentDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocumentById(String id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return DocumentDto.fromEntity(doc);
    }

    @Transactional(readOnly = true)
    public Resource getFileAsResource(String id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        try {
            Path path = Paths.get(doc.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "path", doc.getFilePath());
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File", "id", id);
        }
    }
}
