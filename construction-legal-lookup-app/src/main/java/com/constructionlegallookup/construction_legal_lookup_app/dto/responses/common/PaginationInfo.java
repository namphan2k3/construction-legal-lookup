package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationInfo {
    int page;
    int size;
    long totalElements;
    long totalPages;
    boolean first;
    boolean last;
}
