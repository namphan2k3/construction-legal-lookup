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
public class CategoryAdminRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 255, message = "Tên danh mục không vượt quá 255 ký tự")
    String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 255, message = "Slug không vượt quá 255 ký tự")
    String slug;

    String description;

    int displayOrder;
}
