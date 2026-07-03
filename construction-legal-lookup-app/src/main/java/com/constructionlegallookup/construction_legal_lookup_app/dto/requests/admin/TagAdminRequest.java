package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagAdminRequest {

    @NotBlank(message = "Tên tag không được để trống")
    @Size(max = 100, message = "Tên tag không vượt quá 100 ký tự")
    String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 100, message = "Slug không vượt quá 100 ký tự")
    String slug;
}
