package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrawlSyncResponse {
    Long crawlLogId;
    String status;
    String message;
    LocalDateTime startedAt;
}
