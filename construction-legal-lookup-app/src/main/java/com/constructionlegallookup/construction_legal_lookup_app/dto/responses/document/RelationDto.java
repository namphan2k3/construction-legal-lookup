package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelationDto {
    Long id;
    Long sourceDocumentId;
    Long targetDocumentId;
    String relationType;
    String note;
    DocumentBriefDto document;       // Public API field
    DocumentBriefDto targetDocument; // Admin API field
}
