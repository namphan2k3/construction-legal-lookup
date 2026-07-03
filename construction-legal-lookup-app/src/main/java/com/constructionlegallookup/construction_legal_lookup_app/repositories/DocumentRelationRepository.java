package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.DocumentRelation;

@Repository
public interface DocumentRelationRepository extends JpaRepository<DocumentRelation, Long> {
    List<DocumentRelation> findBySourceDocumentId(Long sourceDocumentId);
    List<DocumentRelation> findByTargetDocumentId(Long targetDocumentId);
}
