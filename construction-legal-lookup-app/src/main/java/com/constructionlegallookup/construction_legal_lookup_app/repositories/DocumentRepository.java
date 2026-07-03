package com.constructionlegallookup.construction_legal_lookup_app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    Optional<Document> findByIdAndDeletedAtIsNull(Long id);
    Optional<Document> findByDocumentNumberNormalized(String documentNumberNormalized);

    @Query("SELECT COUNT(DISTINCT d.documentType) FROM Document d WHERE d.deletedAt IS NULL")
    long countUniqueDocumentTypes();

    @Query("SELECT COUNT(DISTINCT d.issuingBody) FROM Document d WHERE d.deletedAt IS NULL AND d.issuingBody IS NOT NULL AND d.issuingBody != ''")
    long countUniqueIssuingBodies();

    @Query("SELECT DISTINCT d.issuingBody FROM Document d WHERE d.deletedAt IS NULL AND d.issuingBody IS NOT NULL AND d.issuingBody != ''")
    List<String> findDistinctIssuingBodies();

    @Query("SELECT d.documentType, COUNT(d) FROM Document d WHERE d.deletedAt IS NULL GROUP BY d.documentType")
    List<Object[]> countDocumentsGroupedByType();

    @Query("SELECT d.status, COUNT(d) FROM Document d WHERE d.deletedAt IS NULL GROUP BY d.status")
    List<Object[]> countDocumentsGroupedByStatus();

    @Query("SELECT d.issuingBody, COUNT(d) FROM Document d WHERE d.deletedAt IS NULL AND d.issuingBody IS NOT NULL GROUP BY d.issuingBody")
    List<Object[]> countDocumentsGroupedByIssuingBody();

    @Query("SELECT DISTINCT YEAR(d.issuedDate) FROM Document d WHERE d.deletedAt IS NULL ORDER BY YEAR(d.issuedDate) DESC")
    List<Integer> findDistinctYears();

    @Query("SELECT d FROM Document d WHERE d.deletedAt IS NULL AND (LOWER(d.documentNumberNormalized) LIKE :pattern OR LOWER(d.title) LIKE :pattern) ORDER BY CASE WHEN LOWER(d.documentNumberNormalized) LIKE :prefixPattern THEN 0 ELSE 1 END ASC, d.viewCount DESC")
    List<Document> suggestDocuments(@Param("pattern") String pattern, @Param("prefixPattern") String prefixPattern, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.deletedAt IS NULL ORDER BY d.viewCount DESC")
    List<Document> findTopDocuments(Pageable pageable);

    long countByDeletedAtIsNull();
}
