package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    String documentType;
    String status;
    LocalDate issuedDate;
    LocalDateTime deletedAt;
    String sourceUrl;
}
