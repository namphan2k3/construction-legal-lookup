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
public class AiExplainResponse {
    Long documentId;
    String selectedText;
    String explanation;
    List<SourceItem> sources;
    String model;
    String disclaimer;
}
