package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai.AiAskRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.ai.AiExplainRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.ai.*;
import com.constructionlegallookup.construction_legal_lookup_app.entities.Document;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.AppException;
import com.constructionlegallookup.construction_legal_lookup_app.exceptions.ErrorCode;
import com.constructionlegallookup.construction_legal_lookup_app.repositories.DocumentRepository;
import com.constructionlegallookup.construction_legal_lookup_app.services.AiService;
import com.constructionlegallookup.construction_legal_lookup_app.services.GeminiService;
import com.constructionlegallookup.construction_legal_lookup_app.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional(readOnly = true)
public class AiServiceImpl implements AiService {

    private static final String AI_RATE_LIMIT_KEY_PREFIX = "ai:rate:";
    private static final int DAILY_LIMIT = 20;
    private static final String MODEL = "gemini-2.0-flash";
    private static final String DISCLAIMER = "Thông tin do AI tổng hợp, chỉ mang tính tham khảo. Vui lòng đối chiếu văn bản gốc.";

    final DocumentRepository documentRepository;
    final SecurityUtils securityUtils;
    final RedisTemplate<String, String> redisTemplate;
    final GeminiService geminiService;

    @Override
    @Transactional
    public AiSummaryResponse summarizeDocument(Long id) {
        checkRateLimit();

        Document doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_EXISTED));

        String content = doc.getContentText() != null ? doc.getContentText() : doc.getAbstractText();
        if (content == null) {
            content = "Văn bản chưa có nội dung";
        }

        String prompt = String.format("""
                Bạn là một trợ lý pháp lý chuyên nghiệp. Hãy tóm tắt văn bản pháp luật Việt Nam sau đây:

                Thông tin văn bản:
                - Số/ký hiệu: %s
                - Tiêu đề: %s
                - Cơ quan ban hành: %s
                - Ngày ban hành: %s

                Nội dung văn bản:
                %s

                Hãy trả về kết quả dưới dạng JSON với cấu trúc sau:
                {
                    "mainPoints": ["Điểm chính 1", "Điểm chính 2", ...],
                    "applicableSubjects": "Đối tượng áp dụng của văn bản",
                    "mainContent": "Nội dung chính của văn bản (tóm tắt ngắn gọn)"
                }
                Lưu ý: Chỉ trả về JSON, không thêm bất kỳ văn bản nào khác.
                """, doc.getDocumentNumber(), doc.getTitle(), doc.getIssuingBody(), doc.getIssuedDate(), content);

        String aiResponse = callGemini(prompt);

        // Parse AI response (simple approach)
        List<String> mainPoints = new ArrayList<>();
        mainPoints.add("Tóm tắt văn bản: " + doc.getTitle());
        String applicableSubjects = "Đối tượng áp dụng được quy định trong văn bản";
        String mainContent = aiResponse;

        // Try to extract JSON if possible, otherwise use plain text
        if (aiResponse.contains("mainContent")) {
            int start = aiResponse.indexOf("mainContent") + 14;
            int end = aiResponse.indexOf("}", start);
            if (end > start) {
                mainContent = aiResponse.substring(start, end).replace("\"", "").trim();
            }
        }

        List<SourceItem> sources = List.of(
                SourceItem.builder()
                        .documentNumber(doc.getDocumentNumber())
                        .sectionLabel("Toàn văn")
                        .excerpt(content.substring(0, Math.min(200, content.length())))
                        .build()
        );

        AiSummaryResponse.SummaryDetail summary = AiSummaryResponse.SummaryDetail.builder()
                .mainPoints(mainPoints)
                .applicableSubjects(applicableSubjects)
                .mainContent(mainContent)
                .build();

        return AiSummaryResponse.builder()
                .documentId(doc.getId())
                .documentNumber(doc.getDocumentNumber())
                .summary(summary)
                .sources(sources)
                .model(MODEL)
                .disclaimer(DISCLAIMER)
                .build();
    }

    @Override
    @Transactional
    public AiAskResponse askDocument(Long id, AiAskRequest request) {
        checkRateLimit();

        Document doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_EXISTED));

        String content = doc.getContentText() != null ? doc.getContentText() : doc.getAbstractText();
        if (content == null) {
            content = "Văn bản chưa có nội dung";
        }

        String prompt = String.format("""
                Bạn là một trợ lý pháp lý chuyên nghiệp. Dựa trên văn bản pháp luật Việt Nam sau đây, hãy trả lời câu hỏi.

                Thông tin văn bản:
                - Số/ký hiệu: %s
                - Tiêu đề: %s
                - Cơ quan ban hành: %s

                Nội dung văn bản:
                %s

                Câu hỏi: %s

                Hãy trả lời ngắn gọn, chính xác, chỉ dựa trên nội dung văn bản đã cung cấp.
                """, doc.getDocumentNumber(), doc.getTitle(), doc.getIssuingBody(), content, request.getQuestion());

        String answer = callGemini(prompt);

        List<SourceItem> sources = List.of(
                SourceItem.builder()
                        .documentNumber(doc.getDocumentNumber())
                        .sectionLabel("Tham khảo")
                        .excerpt(doc.getAbstractText() != null ? doc.getAbstractText() : content.substring(0, Math.min(200, content.length())))
                        .build()
        );

        return AiAskResponse.builder()
                .documentId(doc.getId())
                .question(request.getQuestion())
                .answer(answer)
                .sources(sources)
                .model(MODEL)
                .disclaimer(DISCLAIMER)
                .build();
    }

    @Override
    @Transactional
    public AiExplainResponse explainText(Long id, AiExplainRequest request) {
        checkRateLimit();

        Document doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_EXISTED));

        String content = doc.getContentText() != null ? doc.getContentText() : doc.getAbstractText();

        String prompt = String.format("""
                Bạn là một trợ lý pháp lý chuyên nghiệp. Hãy giải thích đơn giản, dễ hiểu đoạn văn bản pháp luật sau đây bằng tiếng Việt:

                Văn bản: %s
                Đoạn cần giải thích: %s
                """, doc.getTitle(), request.getSelectedText());

        String explanation = callGemini(prompt);

        List<SourceItem> sources = List.of(
                SourceItem.builder()
                        .documentNumber(doc.getDocumentNumber())
                        .sectionLabel("Đoạn được chọn")
                        .excerpt(request.getSelectedText())
                        .build()
        );

        return AiExplainResponse.builder()
                .documentId(doc.getId())
                .selectedText(request.getSelectedText())
                .explanation(explanation)
                .sources(sources)
                .model(MODEL)
                .disclaimer(DISCLAIMER)
                .build();
    }

    private String callGemini(String prompt) {
        String response = geminiService.generateContent(prompt);
        if (response == null || response.isEmpty()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR);
        }
        return response;
    }

    @Override
    public AiQuotaResponse getQuota() {
        Long userId = securityUtils.getCurrentUser().getId();
        String today = LocalDate.now().toString();
        String key = AI_RATE_LIMIT_KEY_PREFIX + userId + ":" + today;
        String usedStr = redisTemplate.opsForValue().get(key);
        int used = usedStr != null ? Integer.parseInt(usedStr) : 0;

        LocalDateTime resetAt = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);

        return AiQuotaResponse.builder()
                .dailyLimit(DAILY_LIMIT)
                .usedToday(used)
                .remaining(Math.max(0, DAILY_LIMIT - used))
                .resetAt(resetAt)
                .build();
    }

    private void checkRateLimit() {
        Long userId = securityUtils.getCurrentUser().getId();
        String today = LocalDate.now().toString();
        String key = AI_RATE_LIMIT_KEY_PREFIX + userId + ":" + today;
        String usedStr = redisTemplate.opsForValue().get(key);
        int used = usedStr != null ? Integer.parseInt(usedStr) : 0;

        if (used >= DAILY_LIMIT) {
            throw new AppException(ErrorCode.AI_RATE_LIMIT_EXCEEDED);
        }

        // Increment usage
        redisTemplate.opsForValue().increment(key);
        // Set expiration for the key (until end of day)
        long secondsUntilMidnight = LocalDateTime.now().until(
                LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN),
                java.time.temporal.ChronoUnit.SECONDS
        );
        redisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
    }
}
