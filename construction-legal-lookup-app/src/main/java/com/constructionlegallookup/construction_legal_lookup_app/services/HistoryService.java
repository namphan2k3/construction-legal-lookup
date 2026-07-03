package com.constructionlegallookup.construction_legal_lookup_app.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.SearchHistoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.ViewHistoryResponse;

public interface HistoryService {
    Page<SearchHistoryResponse> getSearchHistory(Pageable pageable);
    void deleteSearchHistory();
    Page<ViewHistoryResponse> getViewHistory(Pageable pageable);
    void deleteViewHistory();
}
