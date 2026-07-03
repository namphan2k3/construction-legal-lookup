package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.bookmark;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookmarkStatusResponse {
    Long documentId;
    boolean bookmarked;
}
