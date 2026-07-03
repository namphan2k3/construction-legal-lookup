package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth;

import java.util.Date;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.user.UserResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    UserResponse user;
    String accessToken;
    Date expiresAt;
}
