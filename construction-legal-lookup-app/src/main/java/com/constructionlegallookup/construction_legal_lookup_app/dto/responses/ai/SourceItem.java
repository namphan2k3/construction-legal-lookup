package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SourceItem {
    String documentNumber;
    String sectionLabel;
    String excerpt;
}
