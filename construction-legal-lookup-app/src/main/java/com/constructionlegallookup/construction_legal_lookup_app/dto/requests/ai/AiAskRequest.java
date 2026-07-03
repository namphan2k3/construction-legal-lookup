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
public class AiAskRequest {

    @NotBlank(message = "Câu hỏi không được để trống")
    @Size(min = 5, max = 500, message = "Câu hỏi phải từ 5 đến 500 ký tự")
    String question;
}
