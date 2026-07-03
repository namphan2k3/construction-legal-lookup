package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark;

import java.time.LocalDateTime;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.DocumentBriefDto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookmarkResponse {
    Long id;
    DocumentBriefDto document;
    LocalDateTime createdAt;
}
