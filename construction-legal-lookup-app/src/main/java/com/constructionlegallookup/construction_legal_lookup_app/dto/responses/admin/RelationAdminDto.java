package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelationAdminDto {
    Long id;
    Long sourceDocumentId;
    Long targetDocumentId;
    String relationType;
    String note;
    TargetDocumentBrief targetDocument;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TargetDocumentBrief {
        Long id;
        String documentNumber;
        String title;
    }
}
