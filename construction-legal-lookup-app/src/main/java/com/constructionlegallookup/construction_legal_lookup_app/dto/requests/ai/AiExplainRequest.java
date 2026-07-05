package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai;

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
public class AiExplainRequest {

    @NotBlank(message = "Đoạn văn bản chọn giải thích không được để trống")
    @Size(max = 3000, message = "Đoạn văn bản chọn không được quá 3000 ký tự")
    String selectedText;

    @NotBlank(message = "Câu hỏi không được để trống")
    @Size(max = 500, message = "Câu hỏi không được quá 500 ký tự")
    String question;

    String context;
}
