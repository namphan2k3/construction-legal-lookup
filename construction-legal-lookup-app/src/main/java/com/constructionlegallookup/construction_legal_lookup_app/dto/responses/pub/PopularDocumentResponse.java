package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PopularDocumentResponse {
    Long id;
    String documentNumber;
    String title;
    String documentType;
    int viewCount;
    LocalDate issuedDate;
    String status;
}
