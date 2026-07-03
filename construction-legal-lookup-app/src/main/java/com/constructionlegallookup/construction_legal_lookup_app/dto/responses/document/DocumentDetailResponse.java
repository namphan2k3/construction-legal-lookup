package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
public class DocumentDetailResponse {
    Long id;
    String documentNumber;
    String title;

    @JsonProperty("abstract")
    String abstractText;

    String documentType;
    String issuingBody;
    String signer;
    LocalDate issuedDate;
    LocalDate effectiveDate;
    LocalDate expiryDate;
    String status;
    String field;
    String pdfUrl;
    String pdfFileName;
    Long pdfSizeBytes;
    String sourceUrl;
    String contentText;
    String contentHtml;
    int viewCount;
    int downloadCount;
    List<CategoryResponse> categories;
    List<TagResponse> tags;
    boolean bookmarked;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
