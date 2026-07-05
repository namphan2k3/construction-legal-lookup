package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.*;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.*;
import com.constructionlegallookup.construction_legal_lookup_app.enums.AuditEventType;
import com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentStatus;
import com.constructionlegallookup.construction_legal_lookup_app.enums.DocumentType;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.DocumentMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.*;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.specs.DocumentSpecifications;
import com.constructionlegallookup.construction_legal_lookup_app.services.DocumentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    DocumentRepository documentRepository;
    BookmarkRepository bookmarkRepository;
    SearchHistoryRepository searchHistoryRepository;
    ViewHistoryRepository viewHistoryRepository;
    AuditLogRepository auditLogRepository;
    DocumentMapper documentMapper;

    @Override
    @Transactional
    public Page<DocumentSearchResponse> searchDocuments(
            String q, String documentNumber, String type, String issuingBody,
            Integer year, String status, Long categoryId, Long tagId,
            LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {

        Specification<Document> spec = Specification.where(DocumentSpecifications.isNotDeleted());

        if (q != null && !q.isBlank()) spec = spec.and(DocumentSpecifications.hasKeyword(q));
        if (documentNumber != null && !documentNumber.isBlank()) spec = spec.and(DocumentSpecifications.hasDocumentNumber(documentNumber));
        if (type != null && !type.isBlank()) spec = spec.and(DocumentSpecifications.hasDocumentType(type));
        if (status != null && !status.isBlank()) spec = spec.and(DocumentSpecifications.hasStatus(status));
        if (issuingBody != null && !issuingBody.isBlank()) spec = spec.and(DocumentSpecifications.hasIssuingBody(issuingBody));
        if (year != null) spec = spec.and(DocumentSpecifications.hasYear(year));
        if (categoryId != null) spec = spec.and(DocumentSpecifications.hasCategory(categoryId));
        if (tagId != null) spec = spec.and(DocumentSpecifications.hasTag(tagId));
        if (dateFrom != null) spec = spec.and(DocumentSpecifications.isIssuedAfter(dateFrom));
        if (dateTo != null) spec = spec.and(DocumentSpecifications.isIssuedBefore(dateTo));

        Page<Document> docs = documentRepository.findAll(spec, pageable);
        Long userId = getCurrentUserIdOrNull();

        // Audit log search
        logSearchAudit(userId, q, type, status, issuingBody, year, categoryId, tagId, docs.getNumberOfElements());

        return docs.map(doc -> {
            DocumentSearchResponse response = documentMapper.toDocumentSearchResponse(doc);
            if (q != null && !q.isBlank()) {
                // simple relevance scoring based on match location
                double score = 1.0;
                if (doc.getTitle().toLowerCase().contains(q.toLowerCase())) score += 5.0;
                if (doc.getDocumentNumber().toLowerCase().contains(q.toLowerCase())) score += 10.0;
                response.setRelevanceScore(score);

                // Highlight abstract excerpt
                Map<String, String> highlight = new HashMap<>();
                highlight.put("title", highlightText(doc.getTitle(), q));
                if (doc.getAbstractText() != null) {
                    highlight.put("abstract", highlightText(doc.getAbstractText(), q));
                }
                response.setHighlight(highlight);
            }
            return response;
        });
    }

    @Override
    public List<SuggestResponse> suggestDocuments(String q, Integer limit) {
        if (q == null || q.isBlank() || q.trim().length() < 2) {
            return Collections.emptyList();
        }
        int finalLimit = limit == null ? 8 : limit;
        String pattern = "%" + q.trim().toLowerCase() + "%";
        String prefixPattern = q.trim().toLowerCase() + "%";

        List<Document> docs = documentRepository.suggestDocuments(pattern, prefixPattern, PageRequest.of(0, finalLimit));
        return docs.stream().map(doc -> SuggestResponse.builder()
                .id(doc.getId())
                .documentNumber(doc.getDocumentNumber())
                .title(doc.getTitle())
                .documentType(doc.getDocumentType().name())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentDetailResponse getDocumentDetail(Long id, String highlightKeyword) {
        Document doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy văn bản với id=" + id));

        Long userId = getCurrentUserIdOrNull();

        // Save view history if logged in
        if (userId != null) {
            // Check if user already viewed this document recently (within 1 minute)
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            boolean recentView = viewHistoryRepository.existsByUserIdAndDocumentIdAndCreatedAtAfter(userId, id, oneMinuteAgo);
            
            if (!recentView) {
                // Only increment view count if not viewed recently
                doc.setViewCount(doc.getViewCount() + 1);
                documentRepository.save(doc);
                
                ViewHistory history = ViewHistory.builder()
                        .user(User.builder().id(userId).build())
                        .document(doc)
                        .build();
                viewHistoryRepository.save(history);
            }
        } else {
            // For anonymous users, always increment view count
            doc.setViewCount(doc.getViewCount() + 1);
            documentRepository.save(doc);
        }

        // Save audit log
        logAudit(userId, AuditEventType.VIEW, Map.of("documentId", doc.getId(), "documentNumber", doc.getDocumentNumber()));

        DocumentDetailResponse response = documentMapper.toDocumentDetailResponse(doc);

        // Generate contentHtml fallback if missing
        if (response.getContentText() != null && response.getContentHtml() == null) {
            String html = "<p>" + response.getContentText().replace("\n", "</p><p>") + "</p>";
            response.setContentHtml(html);
        }

        // Process highlight
        if (highlightKeyword != null && !highlightKeyword.isBlank()) {
            response.setTitle(highlightText(response.getTitle(), highlightKeyword));
            if (response.getAbstractText() != null) {
                response.setAbstractText(highlightText(response.getAbstractText(), highlightKeyword));
            }
            if (response.getContentText() != null) {
                response.setContentText(highlightText(response.getContentText(), highlightKeyword));
            }
            if (response.getContentHtml() != null) {
                response.setContentHtml(highlightText(response.getContentHtml(), highlightKeyword));
            }
        }

        // Check bookmark status
        if (userId != null) {
            response.setBookmarked(bookmarkRepository.existsByUserIdAndDocumentId(userId, id));
        }

        return response;
    }

    @Override
    @Transactional
    public String downloadDocumentPdf(Long id) {
        Document doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy văn bản với id=" + id));

        doc.setDownloadCount(doc.getDownloadCount() + 1);
        documentRepository.save(doc);

        Long userId = getCurrentUserIdOrNull();
        logAudit(userId, AuditEventType.DOWNLOAD, Map.of("documentId", doc.getId(), "documentNumber", doc.getDocumentNumber()));

        return doc.getPdfUrl();
    }

    @Override
    public DocumentFilterResponse getFilters() {
        List<Object[]> typeCounts = documentRepository.countDocumentsGroupedByType();
        List<Object[]> statusCounts = documentRepository.countDocumentsGroupedByStatus();
        List<Object[]> bodyCounts = documentRepository.countDocumentsGroupedByIssuingBody();
        List<Integer> years = documentRepository.findDistinctYears();

        List<DocumentFilterResponse.FilterItem> types = typeCounts.stream().map(row -> {
            DocumentType t = (DocumentType) row[0];
            return DocumentFilterResponse.FilterItem.builder()
                    .value(t.name())
                    .label(t.getDisplayName())
                    .count((Long) row[1])
                    .build();
        }).collect(Collectors.toList());

        List<DocumentFilterResponse.FilterItem> statuses = statusCounts.stream().map(row -> {
            DocumentStatus s = (DocumentStatus) row[0];
            return DocumentFilterResponse.FilterItem.builder()
                    .value(s.name())
                    .label(s.getDisplayName())
                    .count((Long) row[1])
                    .build();
        }).collect(Collectors.toList());

        List<DocumentFilterResponse.FilterItem> bodies = bodyCounts.stream().map(row -> {
            String b = (String) row[0];
            return DocumentFilterResponse.FilterItem.builder()
                    .value(b)
                    .label(b)
                    .count((Long) row[1])
                    .build();
        }).collect(Collectors.toList());

        return DocumentFilterResponse.builder()
                .documentTypes(types)
                .statuses(statuses)
                .issuingBodies(bodies)
                .years(years)
                .build();
    }

    private Long getCurrentUserIdOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return Long.valueOf(auth.getName());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void logSearchAudit(Long userId, String q, String type, String status, String body, Integer year, Long catId, Long tagId, int results) {
        Map<String, Object> params = new HashMap<>();
        if (q != null) params.put("q", q);
        if (type != null) params.put("type", type);
        if (status != null) params.put("status", status);
        if (body != null) params.put("issuingBody", body);
        if (year != null) params.put("year", year);
        if (catId != null) params.put("categoryId", catId);
        if (tagId != null) params.put("tagId", tagId);

        // Save SearchHistory if user logged in
        if (userId != null && q != null && !q.isBlank()) {
            try {
                // Check if user already searched with same query recently (within 1 minute)
                LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
                boolean recentSearch = searchHistoryRepository.existsByUserIdAndQueryAndCreatedAtAfter(userId, q, oneMinuteAgo);
                
                if (!recentSearch) {
                    String filtersJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(params);
                    SearchHistory history = SearchHistory.builder()
                            .user(User.builder().id(userId).build())
                            .query(q)
                            .filtersJson(filtersJson)
                            .resultCount(results)
                            .build();
                    searchHistoryRepository.save(history);
                }
            } catch (Exception ignored) {}
        }

        logAudit(userId, AuditEventType.SEARCH, Map.of("query", q != null ? q : "", "results", results, "filters", params));
    }

    private void logAudit(Long userId, AuditEventType eventType, Map<String, Object> metadata) {
        try {
            String metadataJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(metadata);
            AuditLog log = AuditLog.builder()
                    .user(userId != null ? User.builder().id(userId).build() : null)
                    .eventType(eventType)
                    .metadataJson(metadataJson)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ignored) {}
    }

    private String highlightText(String text, String keyword) {
        if (text == null || keyword == null || keyword.isBlank()) return text;
        return text.replaceAll("(?i)" + java.util.regex.Pattern.quote(keyword), "<mark>$0</mark>");
    }
}
