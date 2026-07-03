package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.AdminDashboardResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.AuditLog;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;
import com.constructionlegallookup.construction_legal_lookup_app.enums.AuditEventType;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.AuditLogRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.DocumentRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.SearchHistoryRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.UserRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AdminService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    DocumentRepository documentRepository;
    UserRepository userRepository;
    SearchHistoryRepository searchHistoryRepository;
    AuditLogRepository auditLogRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse getDashboard(String period) {
        LocalDateTime startDate = switch (period) {
            case "30d" -> LocalDateTime.now().minusDays(30);
            case "all" -> LocalDateTime.MIN;
            default -> LocalDateTime.now().minusDays(7);
        };

        List<AuditLog> auditLogs = auditLogRepository.findByCreatedAtAfter(startDate);

        long totalSearches = auditLogs.stream()
                .filter(log -> log.getEventType() == AuditEventType.SEARCH)
                .count();
        long totalViews = auditLogs.stream()
                .filter(log -> log.getEventType() == AuditEventType.VIEW)
                .count();
        long totalDownloads = auditLogs.stream()
                .filter(log -> log.getEventType() == AuditEventType.DOWNLOAD)
                .count();
        long totalAiRequests = auditLogs.stream()
                .filter(log -> log.getEventType() == AuditEventType.AI_REQUEST)
                .count();

        List<Object[]> topKeywordResults = searchHistoryRepository.findTopKeywords(startDate, PageRequest.of(0, 10));
        List<AdminDashboardResponse.KeywordItem> topKeywords = topKeywordResults.stream()
                .map(row -> AdminDashboardResponse.KeywordItem.builder()
                        .keyword((String) row[0])
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        List<Document> topDocResults = documentRepository.findTopDocuments(PageRequest.of(0, 10));
        List<AdminDashboardResponse.PopularDocItem> topDocuments = topDocResults.stream()
                .map(doc -> AdminDashboardResponse.PopularDocItem.builder()
                        .id(doc.getId())
                        .documentNumber(doc.getDocumentNumber())
                        .title(doc.getTitle())
                        .viewCount(doc.getViewCount())
                        .build())
                .collect(Collectors.toList());

        List<AdminDashboardResponse.ActivityItem> recentActivities = auditLogs.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(log -> AdminDashboardResponse.ActivityItem.builder()
                        .eventType(log.getEventType().name())
                        .metadata(parseMetadata(log.getMetadataJson()))
                        .createdAt(log.getCreatedAt())
                        .build())
                .limit(10)
                .collect(Collectors.toList());

        AdminDashboardResponse.OverviewStats overview = AdminDashboardResponse.OverviewStats.builder()
                .totalDocuments(documentRepository.countByDeletedAtIsNull())
                .totalUsers(userRepository.count())
                .totalSearches(totalSearches)
                .totalViews(totalViews)
                .totalDownloads(totalDownloads)
                .totalAiRequests(totalAiRequests)
                .build();

        return AdminDashboardResponse.builder()
                .overview(overview)
                .topKeywords(topKeywords)
                .topDocuments(topDocuments)
                .recentActivity(recentActivities)
                .period(period)
                .build();
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
