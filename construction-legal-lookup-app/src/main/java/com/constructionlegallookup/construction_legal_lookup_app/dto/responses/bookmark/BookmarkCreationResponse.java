package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookmarkCreationResponse {
    Long id;
    Long documentId;
    LocalDateTime createdAt;
}
