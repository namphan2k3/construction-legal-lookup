package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.DocumentSection;

@Repository
public interface DocumentSectionRepository extends JpaRepository<DocumentSection, Long> {
    List<DocumentSection> findByDocumentIdOrderByOrderIndexAsc(Long documentId);
}
