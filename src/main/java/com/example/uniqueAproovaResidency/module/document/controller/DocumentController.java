package com.example.uniqueAproovaResidency.module.document.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.document.dto.DocumentDto;
import com.example.uniqueAproovaResidency.module.document.service.DocumentService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document / Proof Management", description = "Endpoints for uploading and retrieving invoice, receipt, and meter photo proofs")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Document Proof")
    public ResponseEntity<ApiResponse<DocumentDto>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "relatedEntityType", required = false) String relatedEntityType,
            @RequestParam(value = "relatedEntityId", required = false) String relatedEntityId,
            @AuthenticationPrincipal UserPrincipal currentUser) throws IOException {
        User uploader = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        DocumentDto dto = documentService.uploadDocument(file, documentType, relatedEntityType, relatedEntityId, uploader);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Document uploaded successfully", dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Document Metadata by ID")
    public ResponseEntity<ApiResponse<DocumentDto>> getDocument(@PathVariable String id) {
        DocumentDto dto = documentService.getDocumentById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{id}/file")
    @Operation(summary = "Download / View File Content Bytes")
    public ResponseEntity<Resource> getDocumentFile(@PathVariable String id) {
        DocumentDto doc = documentService.getDocumentById(id);
        Resource resource = documentService.getFileAsResource(id);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(doc.getFileType() != null ? doc.getFileType() : "image/jpeg");
        } catch (Exception e) {
            mediaType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }
}
