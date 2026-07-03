package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRoleRequest {

    @NotBlank(message = "Vai trò không được để trống")
    String role; // "USER" | "ADMIN"
}
