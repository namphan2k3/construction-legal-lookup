package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.document.*;
import com.constructionlegallookup.construction_legal_lookup_app.services.DocumentService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;
import com.constructionlegallookup.construction_legal_lookup_app.utils.MessageKeys;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("/search")
    public ApiResponse<Page<DocumentSearchResponse>> searchDocuments(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String issuingBody,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<DocumentSearchResponse> page = documentService.searchDocuments(
                q, documentNumber, type, issuingBody, year, status, categoryId, tagId, dateFrom, dateTo, pageable);
        return ApiResponse.<Page<DocumentSearchResponse>>builder()
                .data(page)
                .build();
    }

    @GetMapping("/suggest")
    public ApiResponse<List<SuggestResponse>> suggestDocuments(
            @RequestParam String q,
            @RequestParam(defaultValue = "8") Integer limit) {
        List<SuggestResponse> suggestions = documentService.suggestDocuments(q, limit);
        return ApiResponse.<List<SuggestResponse>>builder()
                .data(suggestions)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentDetailResponse> getDocumentDetail(
            @PathVariable Long id,
            @RequestParam(required = false) String highlight) {
        DocumentDetailResponse detail = documentService.getDocumentDetail(id, highlight);
        return ApiResponse.<DocumentDetailResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.DOCUMENT_DETAIL_SUCCESS))
                .data(detail)
                .build();
    }

    @GetMapping("/{id}/sections")
    public ApiResponse<List<SectionDto>> getDocumentSections(@PathVariable Long id) {
        List<SectionDto> sections = documentService.getDocumentSections(id);
        return ApiResponse.<List<SectionDto>>builder()
                .data(sections)
                .build();
    }

    @GetMapping("/{id}/relations")
    public ApiResponse<DocumentRelationsResponse> getDocumentRelations(@PathVariable Long id) {
        DocumentRelationsResponse relations = documentService.getDocumentRelations(id);
        return ApiResponse.<DocumentRelationsResponse>builder()
                .data(relations)
                .build();
    }

    @GetMapping("/{id}/download")
    public ApiResponse<String> downloadDocument(@PathVariable Long id) {
        String downloadUrl = documentService.downloadDocumentPdf(id);
        return ApiResponse.<String>builder()
                .data(downloadUrl)
                .build();
    }

    @GetMapping("/filters")
    public ApiResponse<DocumentFilterResponse> getFilters() {
        DocumentFilterResponse filters = documentService.getFilters();
        return ApiResponse.<DocumentFilterResponse>builder()
                .data(filters)
                .build();
    }
}
