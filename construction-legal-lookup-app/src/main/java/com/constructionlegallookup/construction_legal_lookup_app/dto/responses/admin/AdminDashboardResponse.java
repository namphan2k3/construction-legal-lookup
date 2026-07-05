package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.DocumentBriefDto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminDashboardResponse {
    OverviewStats overview;
    List<KeywordItem> topKeywords;
    List<PopularDocItem> topDocuments;
    List<ActivityItem> recentActivity;
    String period;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OverviewStats {
        long totalDocuments;
        long totalUsers;
        long totalSearches;
        long totalViews;
        long totalDownloads;
        long totalAiRequests;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class KeywordItem {
        String keyword;
        long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PopularDocItem {
        Long id;
        String documentNumber;
        String title;
        long viewCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ActivityItem {
        String eventType;
        Map<String, Object> metadata;
        LocalDateTime createdAt;
        AdminUserDto user;
    }
}
