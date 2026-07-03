package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiSummaryResponse {
    Long documentId;
    String documentNumber;
    SummaryDetail summary;
    List<SourceItem> sources;
    String model;
    String disclaimer;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SummaryDetail {
        List<String> mainPoints;
        String applicableSubjects;
        String mainContent;
    }
}
