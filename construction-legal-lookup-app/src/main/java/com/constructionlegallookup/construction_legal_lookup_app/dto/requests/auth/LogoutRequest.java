package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogoutRequest {

    @NotBlank(message = "Access token không được để trống")
    String accessToken;
}
