package com.constructionlegallookup.construction_legal_lookup_app.mappers;

import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.SearchHistoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.ViewHistoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.SearchHistory;
import com.constructionlegallookup.construction_legal_lookup_app.entities.ViewHistory;

@Mapper(componentModel = "spring", uses = {DocumentMapper.class})
public interface HistoryMapper {

    @Mapping(target = "filters", source = "filtersJson")
    SearchHistoryResponse toSearchHistoryResponse(SearchHistory searchHistory);

    @Mapping(target = "viewedAt", source = "createdAt")
    ViewHistoryResponse toViewHistoryResponse(ViewHistory viewHistory);

    default Map<String, Object> mapFilters(String filtersJson) {
        if (filtersJson == null || filtersJson.isBlank()) {
            return java.util.Collections.emptyMap();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                filtersJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }
}
