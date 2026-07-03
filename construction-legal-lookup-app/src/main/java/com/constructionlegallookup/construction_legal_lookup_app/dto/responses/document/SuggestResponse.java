package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestResponse {
    Long id;
    String documentNumber;
    String title;
    String documentType;
}
