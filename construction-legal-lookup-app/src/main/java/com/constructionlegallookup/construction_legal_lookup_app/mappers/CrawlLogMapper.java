package com.constructionlegallookup.construction_legal_lookup_app.mappers;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.CrawlLogResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.CrawlLog;

@Mapper(componentModel = "spring")
public interface CrawlLogMapper {

    @Mapping(target = "errorDetails", source = "errorDetails")
    CrawlLogResponse toCrawlLogResponse(CrawlLog crawlLog);

    default List<Map<String, Object>> mapErrorDetails(String errorDetailsJson) {
        if (errorDetailsJson == null || errorDetailsJson.isBlank()) {
            return java.util.Collections.emptyList();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                errorDetailsJson,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
