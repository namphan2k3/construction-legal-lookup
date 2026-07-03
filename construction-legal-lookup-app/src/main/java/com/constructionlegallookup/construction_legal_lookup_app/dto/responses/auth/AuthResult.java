package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.user.UserResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResult {
    UserResponse user;
    TokenInfo tokenInfo;
}
