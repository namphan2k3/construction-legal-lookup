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
public class SectionDto {
    Long id;
    String sectionType;
    String numberLabel;
    String title;
    String content;
    int orderIndex;
    String anchorSlug;
    List<SectionDto> children;
}
