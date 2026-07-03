package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadPdfResponse {
    Long documentId;
    String pdfUrl;
    String pdfFileName;
    Long pdfSizeBytes;
    boolean contentTextExtracted;
    int contentTextLength;
}
