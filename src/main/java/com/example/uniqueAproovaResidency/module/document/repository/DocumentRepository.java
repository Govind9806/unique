package com.example.uniqueAproovaResidency.module.document.repository;

import com.example.uniqueAproovaResidency.module.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findByRelatedEntityTypeAndRelatedEntityId(String relatedEntityType, String relatedEntityId);
}
