package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrawlLogResponse {
    Long id;
    String status;
    String triggeredBy;
    Long triggeredByUserId;
    LocalDateTime startedAt;
    LocalDateTime finishedAt;
    int insertedCount;
    int updatedCount;
    int skippedCount;
    int errorCount;
    List<Map<String, Object>> errorDetails;
    String notes;
}
