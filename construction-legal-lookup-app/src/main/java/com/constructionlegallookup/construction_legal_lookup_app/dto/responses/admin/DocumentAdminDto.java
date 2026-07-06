package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.TagResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentAdminDto {
    Long id;
    String documentNumber;
    String title;
    String abstractText;
    String documentType;
    String status;
    String issuingBody;
    String signer;
    LocalDate issuedDate;
    LocalDate effectiveDate;
    LocalDate expiryDate;
    String sourceUrl;
    String contentText;
    String contentHtml;
    List<CategoryResponse> categories;
    List<TagResponse> tags;
    LocalDateTime deletedAt;
}
