package com.constructionlegallookup.construction_legal_lookup_app.services;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.GeneralChatRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai.AiAskRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai.AiExplainRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.GeneralChatResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai.AiAskResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai.AiExplainResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai.AiQuotaResponse;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai.AiSummaryResponse;

public interface AiService {
    AiSummaryResponse summarizeDocument(Long id);
    AiAskResponse askDocument(Long id, AiAskRequest request);
    AiExplainResponse explainText(Long id, AiExplainRequest request);
    AiQuotaResponse getQuota();
    GeneralChatResponse generalChat(GeneralChatRequest request);
}
