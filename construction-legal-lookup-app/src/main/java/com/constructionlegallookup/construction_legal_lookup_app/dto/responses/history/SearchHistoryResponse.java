package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchHistoryResponse {
    Long id;
    String query;
    Map<String, Object> filters;
    Integer resultCount;
    LocalDateTime createdAt;
}
