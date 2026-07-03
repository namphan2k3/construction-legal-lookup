package com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsResponse {
    long totalDocuments;
    long totalTypes;
    long totalIssuingBodies;
    LocalDateTime datasetUpdatedAt;
    String datasetLabel;
}
