package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserDto {
    Long id;
    String email;
    String fullName;
    String role;
    boolean enabled;
    LocalDateTime createdAt;
}
