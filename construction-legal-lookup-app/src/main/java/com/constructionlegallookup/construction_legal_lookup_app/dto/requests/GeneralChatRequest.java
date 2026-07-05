package com.constructionlegallookup.construction_legal_lookup_app.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeneralChatRequest {
    @NotBlank(message = "Câu hỏi không được để trống")
    @Size(max = 1000, message = "Câu hỏi không được vượt quá 1000 ký tự")
    String question;
}
