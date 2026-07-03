package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.auth;

import java.util.Date;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenInfo {
    String accessToken;
    String refreshToken;
    Date expiresAt;
    Date refreshExpiresAt;
    long refreshMaxAge;
}
