package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecentDocumentResponse {
    Long id;
    String documentNumber;
    String title;
    String documentType;
    String issuingBody;
    LocalDate issuedDate;
    String status;

    @JsonProperty("abstract")
    String abstractText;

    LocalDateTime updatedAt;
}
