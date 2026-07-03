package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagResponse {
    Long id;
    String name;
    String slug;
}
