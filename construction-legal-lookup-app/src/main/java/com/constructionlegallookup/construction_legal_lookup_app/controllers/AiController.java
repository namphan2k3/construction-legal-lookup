package com.constructionlegallookup.construction_legal_lookup_app.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.GeneralChatRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai.AiAskRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai.AiExplainRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.GeneralChatResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai.*;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.common.ApiResponse;
import com.constructionlegallookup.construction_legal_lookup_app.services.AiService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.LocalizationUtils;
import com.constructionlegallookup.construction_legal_lookup_app.utils.MessageKeys;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("/documents/{id}/summarize")
    public ApiResponse<AiSummaryResponse> summarizeDocument(@PathVariable Long id) {
        AiSummaryResponse response = aiService.summarizeDocument(id);
        return ApiResponse.<AiSummaryResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/documents/{id}/ask")
    public ApiResponse<AiAskResponse> askDocument(
            @PathVariable Long id,
            @Valid @RequestBody AiAskRequest request) {
        AiAskResponse response = aiService.askDocument(id, request);
        return ApiResponse.<AiAskResponse>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.AI_ASK_SUCCESS))
                .data(response)
                .build();
    }

    @PostMapping("/documents/{id}/explain")
    public ApiResponse<AiExplainResponse> explainText(
            @PathVariable Long id,
            @Valid @RequestBody AiExplainRequest request) {
        AiExplainResponse response = aiService.explainText(id, request);
        return ApiResponse.<AiExplainResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/quota")
    public ApiResponse<AiQuotaResponse> getQuota() {
        AiQuotaResponse response = aiService.getQuota();
        return ApiResponse.<AiQuotaResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/general-chat")
    public ApiResponse<GeneralChatResponse> generalChat(@Valid @RequestBody GeneralChatRequest request) {
        GeneralChatResponse response = aiService.generalChat(request);
        return ApiResponse.<GeneralChatResponse>builder()
                .data(response)
                .build();
    }
}
