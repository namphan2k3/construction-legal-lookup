package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.CategoryResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.PopularDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.RecentDocumentResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.pub.StatsResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.PublicService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("/stats")
    public ApiResponse<StatsResponse> getStats() {
        StatsResponse stats = publicService.getStats();
        return ApiResponse.<StatsResponse>builder()
                .data(stats)
                .build();
    }

    @GetMapping("/documents/recent")
    public ApiResponse<List<RecentDocumentResponse>> getRecentDocuments(@RequestParam(defaultValue = "10") int limit) {
        List<RecentDocumentResponse> docs = publicService.getRecentDocuments(limit);
        return ApiResponse.<List<RecentDocumentResponse>>builder()
                .data(docs)
                .build();
    }

    @GetMapping("/documents/popular")
    public ApiResponse<List<PopularDocumentResponse>> getPopularDocuments(@RequestParam(defaultValue = "10") int limit) {
        List<PopularDocumentResponse> docs = publicService.getPopularDocuments(limit);
        return ApiResponse.<List<PopularDocumentResponse>>builder()
                .data(docs)
                .build();
    }

    @GetMapping("/documents/updated")
    public ApiResponse<List<RecentDocumentResponse>> getUpdatedDocuments(@RequestParam(defaultValue = "10") int limit) {
        List<RecentDocumentResponse> docs = publicService.getUpdatedDocuments(limit);
        return ApiResponse.<List<RecentDocumentResponse>>builder()
                .data(docs)
                .build();
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> categories = publicService.getCategories();
        return ApiResponse.<List<CategoryResponse>>builder()
                .data(categories)
                .build();
    }
}
