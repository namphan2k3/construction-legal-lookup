package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentSearchResponse {
    Long id;
    String documentNumber;
    String title;

    @JsonProperty("abstract")
    String abstractText;

    String documentType;
    String issuingBody;
    LocalDate issuedDate;
    LocalDate effectiveDate;
    String status;
    int viewCount;
    List<CategoryResponse> categories;
    Map<String, String> highlight;
    Double relevanceScore;
}
