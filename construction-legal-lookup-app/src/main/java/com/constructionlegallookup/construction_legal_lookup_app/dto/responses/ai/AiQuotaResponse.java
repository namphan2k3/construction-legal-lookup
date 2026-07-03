package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiQuotaResponse {
    int dailyLimit;
    int usedToday;
    int remaining;
    LocalDateTime resetAt;
}
