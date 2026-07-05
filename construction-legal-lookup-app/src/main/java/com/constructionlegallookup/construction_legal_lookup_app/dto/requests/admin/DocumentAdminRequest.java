package com.constructionlegallookup.construction_legal_lookup_app.dto.requests.admin;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentAdminRequest {

    @NotBlank(message = "Số hiệu văn bản không được để trống")
    @Size(max = 100, message = "Số hiệu văn bản không vượt quá 100 ký tự")
    String documentNumber;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500, message = "Tiêu đề không vượt quá 500 ký tự")
    String title;

    @JsonProperty("abstract")
    String abstractText;

    @NotBlank(message = "Loại văn bản không được để trống")
    String documentType;

    @Size(max = 255, message = "Cơ quan ban hành không vượt quá 255 ký tự")
    String issuingBody;

    @Size(max = 255, message = "Người ký không vượt quá 255 ký tự")
    String signer;

    @NotNull(message = "Ngày ban hành không được để trống")
    LocalDate issuedDate;

    LocalDate effectiveDate;
    LocalDate expiryDate;

    @NotBlank(message = "Trạng thái hiệu lực không được để trống")
    String status;

    String contentText;
    String contentHtml;
    String sourceUrl;
    List<Long> categoryIds;
    List<Long> tagIds;
}
