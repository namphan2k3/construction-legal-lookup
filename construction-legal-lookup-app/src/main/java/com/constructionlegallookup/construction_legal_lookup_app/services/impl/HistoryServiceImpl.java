package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.SearchHistoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.history.ViewHistoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.SearchHistory;
import com.constructionlegallookup.construction_legal_lookup_app.entities.User;
import com.constructionlegallookup.construction_legal_lookup_app.entities.ViewHistory;
import com.constructionlegallookup.construction_legal_lookup_app.mappers.HistoryMapper;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.SearchHistoryRepository;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.ViewHistoryRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.HistoryService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class HistoryServiceImpl implements HistoryService {

    SearchHistoryRepository searchHistoryRepository;
    ViewHistoryRepository viewHistoryRepository;
    HistoryMapper historyMapper;
    SecurityUtils securityUtils;

    @Override
    public Page<SearchHistoryResponse> getSearchHistory(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<SearchHistory> page = searchHistoryRepository.findByUserId(currentUser.getId(), pageable);
        return page.map(historyMapper::toSearchHistoryResponse);
    }

    @Override
    @Transactional
    public void deleteSearchHistory() {
        User currentUser = securityUtils.getCurrentUser();
        searchHistoryRepository.deleteByUserId(currentUser.getId());
    }

    @Override
    public Page<ViewHistoryResponse> getViewHistory(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<ViewHistory> page = viewHistoryRepository.findByUserId(currentUser.getId(), pageable);
        return page.map(historyMapper::toViewHistoryResponse);
    }

    @Override
    @Transactional
    public void deleteViewHistory() {
        User currentUser = securityUtils.getCurrentUser();
        viewHistoryRepository.deleteByUserId(currentUser.getId());
    }
}
