package com.constructionlegallookup.construction_legal_lookup_app.services;

import java.util.List;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.PopularDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.RecentDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.StatsResponse;

public interface PublicService {
    StatsResponse getStats();
    List<RecentDocumentResponse> getRecentDocuments(int limit);
    List<PopularDocumentResponse> getPopularDocuments(int limit);
    List<RecentDocumentResponse> getUpdatedDocuments(int limit);
    List<CategoryResponse> getCategories();
}
