package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    Boolean success = true;

    String message;
    T data;
    Map<String, String> errors;

    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    PaginationInfo paginationInfo;
}
