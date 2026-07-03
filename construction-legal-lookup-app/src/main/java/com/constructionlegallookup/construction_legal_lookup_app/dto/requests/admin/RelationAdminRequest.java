package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelationAdminRequest {

    @NotNull(message = "ID văn bản gốc không được để trống")
    Long sourceDocumentId;

    @NotNull(message = "ID văn bản liên kết không được để trống")
    Long targetDocumentId;

    @NotBlank(message = "Loại liên kết không được để trống")
    String relationType;

    String note;
}
