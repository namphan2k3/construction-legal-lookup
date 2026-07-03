package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiExplainRequest {

    @NotBlank(message = "Đoạn văn bản chọn giải thích không được để trống")
    String selectedText;

    String context;
}
