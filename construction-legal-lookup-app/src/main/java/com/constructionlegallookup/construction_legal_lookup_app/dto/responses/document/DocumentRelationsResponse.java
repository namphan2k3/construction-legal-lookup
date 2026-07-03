package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentRelationsResponse {
    Long documentId;
    RelationsGroup relations;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RelationsGroup {
        List<RelationDto> guidedBy;
        List<RelationDto> guides;
        List<RelationDto> amendedBy;
        List<RelationDto> amends;
        List<RelationDto> replaces;
        List<RelationDto> replacedBy;
        List<RelationDto> related;
    }
}
