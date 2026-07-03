package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrawlSyncRequest {
    String mode;   // "full" | "incremental"
    String source; // "all" | specific source name
}
