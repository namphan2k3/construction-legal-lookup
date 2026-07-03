package com.constructionlegallookup.construction_legal_lookup_app.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.*;

public interface DocumentService {
    Page<DocumentSearchResponse> searchDocuments(
            String q, String documentNumber, String type, String issuingBody,
            Integer year, String status, Long categoryId, Long tagId,
            LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

    List<SuggestResponse> suggestDocuments(String q, Integer limit);

    DocumentDetailResponse getDocumentDetail(Long id, String highlightKeyword);

    DocumentRelationsResponse getDocumentRelations(Long id);

    List<SectionDto> getDocumentSections(Long id);

    String downloadDocumentPdf(Long id); // Returns the PDF URL for redirect

    DocumentFilterResponse getFilters();
}
