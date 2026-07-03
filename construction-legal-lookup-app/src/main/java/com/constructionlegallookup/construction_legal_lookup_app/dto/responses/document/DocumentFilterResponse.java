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
public class DocumentFilterResponse {
    List<FilterItem> documentTypes;
    List<FilterItem> statuses;
    List<FilterItem> issuingBodies;
    List<Integer> years;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FilterItem {
        String value;
        String label;
        long count;
    }
}
