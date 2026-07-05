package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.SearchHistoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.ViewHistoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.HistoryService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;
import com.constructionlegallookup.construction_legal_lookup_app.utils.MessageKeys;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("/search")
    public ApiResponse<Page<SearchHistoryResponse>> getSearchHistory(@PageableDefault(size = 20) Pageable pageable) {
        Page<SearchHistoryResponse> history = historyService.getSearchHistory(pageable);
        return ApiResponse.<Page<SearchHistoryResponse>>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.SEARCH_HISTORY_LIST_SUCCESS))
                .data(history)
                .build();
    }

    @DeleteMapping("/search")
    public ApiResponse<Void> deleteSearchHistory() {
        historyService.deleteSearchHistory();
        return ApiResponse.<Void>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.SEARCH_HISTORY_CLEAR_SUCCESS))
                .build();
    }

    @GetMapping("/views")
    public ApiResponse<Page<ViewHistoryResponse>> getViewHistory(@PageableDefault(size = 20) Pageable pageable) {
        Page<ViewHistoryResponse> history = historyService.getViewHistory(pageable);
        return ApiResponse.<Page<ViewHistoryResponse>>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.VIEW_HISTORY_LIST_SUCCESS))
                .data(history)
                .build();
    }

    @DeleteMapping("/views")
    public ApiResponse<Void> deleteViewHistory() {
        historyService.deleteViewHistory();
        return ApiResponse.<Void>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.VIEW_HISTORY_CLEAR_SUCCESS))
                .build();
    }
}
