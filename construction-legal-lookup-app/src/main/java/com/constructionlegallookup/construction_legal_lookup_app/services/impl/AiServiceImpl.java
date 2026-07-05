package com.constructionlegallookup.construction_legal_lookup_app.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.constructionlegallookup.construction_legal_lookup_app.dto.requests.GeneralChatRequest;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.GeneralChatResponse;
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
    private static final String MODEL = "gemini-3.5-flash";
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
                    "mainPoints": ["Điểm chính 1", "Điểm chính 2", "Điểm chính 3", "Điểm chính 4", "Điểm chính 5"],
                    "applicableSubjects": "Đối tượng áp dụng của văn bản",
                    "mainContent": "Nội dung chính của văn bản (tóm tắt ngắn gọn)"
                }
                YÊU CẦU:
                - Trả về chính xác 5 điểm chính
                - Chỉ trả về JSON, không thêm bất kỳ văn bản nào khác
                - JSON phải valid và có thể parse được
                """, doc.getDocumentNumber(), doc.getTitle(), doc.getIssuingBody(), doc.getIssuedDate(), content);

        String aiResponse = callGemini(prompt);

        // Parse JSON response
        List<String> mainPoints = new ArrayList<>();
        String applicableSubjects = "Đối tượng áp dụng được quy định trong văn bản";
        String mainContent = aiResponse;

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(aiResponse);

            if (jsonNode.has("mainPoints")) {
                com.fasterxml.jackson.databind.JsonNode pointsNode = jsonNode.get("mainPoints");
                if (pointsNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode point : pointsNode) {
                        mainPoints.add(point.asText());
                    }
                }
            }

            if (jsonNode.has("applicableSubjects")) {
                applicableSubjects = jsonNode.get("applicableSubjects").asText();
            }

            if (jsonNode.has("mainContent")) {
                mainContent = jsonNode.get("mainContent").asText();
            }
        } catch (Exception e) {
            // Fallback if JSON parsing fails
            mainPoints.add("Tóm tắt văn bản: " + doc.getTitle());
            mainContent = aiResponse.contains("Không thể tạo phản hồi") ? "Không thể tóm tắt văn bản" : aiResponse;
        }

        // Ensure we have at least 5 main points
        while (mainPoints.size() < 5) {
            mainPoints.add("Thông tin chi tiết xem trong văn bản gốc");
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

                Hãy trả về kết quả dưới dạng JSON với cấu trúc sau:
                {
                    "answer": "Câu trả lời chi tiết",
                    "citations": [
                        {"section": "Điều 1", "excerpt": "Nội dung trích dẫn"},
                        {"section": "Khoản 2", "excerpt": "Nội dung trích dẫn"}
                    ]
                }
                YÊU CẦU:
                - Trả lời ngắn gọn, chính xác, chỉ dựa trên nội dung văn bản đã cung cấp
                - Phải bao gồm trích dẫn Điều/Khoản cụ thể từ văn bản
                - Chỉ trả về JSON, không thêm bất kỳ văn bản nào khác
                - JSON phải valid và có thể parse được
                """, doc.getDocumentNumber(), doc.getTitle(), doc.getIssuingBody(), content, request.getQuestion());

        String aiResponse = callGemini(prompt);

        String answer = aiResponse;
        List<SourceItem> sources = new ArrayList<>();

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(aiResponse);

            if (jsonNode.has("answer")) {
                answer = jsonNode.get("answer").asText();
            }

            if (jsonNode.has("citations")) {
                com.fasterxml.jackson.databind.JsonNode citationsNode = jsonNode.get("citations");
                if (citationsNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode citation : citationsNode) {
                        String section = citation.has("section") ? citation.get("section").asText() : "Tham khảo";
                        String excerpt = citation.has("excerpt") ? citation.get("excerpt").asText() : "";
                        sources.add(SourceItem.builder()
                                .documentNumber(doc.getDocumentNumber())
                                .sectionLabel(section)
                                .excerpt(excerpt)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            // Fallback if JSON parsing fails
            sources.add(SourceItem.builder()
                    .documentNumber(doc.getDocumentNumber())
                    .sectionLabel("Tham khảo")
                    .excerpt(doc.getAbstractText() != null ? doc.getAbstractText() : content.substring(0, Math.min(200, content.length())))
                    .build());
        }

        // Ensure at least one source
        if (sources.isEmpty()) {
            sources.add(SourceItem.builder()
                    .documentNumber(doc.getDocumentNumber())
                    .sectionLabel("Tham khảo")
                    .excerpt(doc.getAbstractText() != null ? doc.getAbstractText() : content.substring(0, Math.min(200, content.length())))
                    .build());
        }

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

        String prompt = String.format("""
                Bạn là trợ lý AI hỗ trợ tra cứu văn bản pháp luật Việt Nam.

                Đây là đoạn văn bản mà người dùng đã chọn:
                "%s"

                Người dùng hỏi:
                "%s"

                YÊU CẦU:
                - Chỉ trả lời dựa trên nội dung được cung cấp.
                - Không tự suy diễn hoặc bổ sung quy định không có trong đoạn văn.
                - Nếu đoạn văn không đủ thông tin để trả lời, hãy nói rõ rằng cần thêm ngữ cảnh hoặc thêm nội dung của văn bản.
                - Trả lời bằng tiếng Việt, rõ ràng, dễ hiểu.
                - Trả về kết quả dưới dạng JSON với cấu trúc sau:
                {
                    "explanation": "Câu trả lời chi tiết",
                    "keyPoints": ["Điểm quan trọng 1", "Điểm quan trọng 2"]
                }
                - Chỉ trả về JSON, không thêm bất kỳ văn bản nào khác
                - JSON phải valid và có thể parse được
                """, request.getSelectedText(), request.getQuestion());

        String aiResponse = callGemini(prompt);

        String explanation = aiResponse;

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(aiResponse);

            if (jsonNode.has("explanation")) {
                explanation = jsonNode.get("explanation").asText();
            }
        } catch (Exception e) {
            // Fallback if JSON parsing fails
            if (aiResponse.contains("Không thể tạo phản hồi")) {
                explanation = "Không thể giải thích đoạn văn bản";
            }
        }

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

    @Override
    public GeneralChatResponse generalChat(GeneralChatRequest request) {
        checkRateLimit();

        // Bước 1: Phân loại câu hỏi có thuộc lĩnh vực xây dựng không
        boolean isRelevant = isQuestionRelevantToConstruction(request.getQuestion());
        if (!isRelevant) {
            return GeneralChatResponse.builder()
                    .answer("Xin lỗi, tôi chỉ hỗ trợ tra cứu và giải đáp pháp luật trong lĩnh vực xây dựng tại Việt Nam. Vui lòng hỏi về các vấn đề liên quan đến luật xây dựng, giấy phép xây dựng, quy hoạch, hợp đồng xây dựng, an toàn lao động xây dựng, PCCC, v.v.")
                    .sources(List.of())
                    .build();
        }

        // Bước 2: Tìm kiếm tài liệu liên quan
        List<Document> relevantDocs = documentRepository.searchRelevantDocuments(
                request.getQuestion(),
                org.springframework.data.domain.PageRequest.of(0, 5)
        );

        // Bước 3: Tạo prompt với context từ các tài liệu liên quan (nếu có)
        StringBuilder contextBuilder = new StringBuilder();
        if (!relevantDocs.isEmpty()) {
            for (Document doc : relevantDocs) {
                contextBuilder.append(String.format("""
                        TÀI LIỆU TRONG HỆ THỐNG: %s
                        SỐ KÝ HIỆU: %s
                        NỘI DUNG: %s
                        
                        """,
                        doc.getTitle(),
                        doc.getDocumentNumber(),
                        doc.getContentText() != null ? doc.getContentText().substring(0, Math.min(3000, doc.getContentText().length())) : ""
                ));
            }
        }

        String hasDocs = !relevantDocs.isEmpty() ? "có" : "không";
        String prompt = String.format("""
                Bạn là trợ lý pháp lý chuyên về lĩnh vực xây dựng tại Việt Nam.

                CÂU HỎI CỦA NGƯỜI DÙNG:
                "%s"

                CÁC TÀI LIỆU TRONG HỆ THỐNG (%s):
                %s

                YÊU CẦU QUAN TRỌNG:
                1. ƯU TIÊN SỬ DỤNG NỘI DUNG TỪ CÁC TÀI LIỆU TRONG HỆ THỐNG ĐỂ TRẢ LỜI.
                2. NẾU KHÔNG CÓ TÀI LIỆU TRONG HỆ THỐNG HOẶC TÀI LIỆU KHÔNG ĐỦ THÔNG TIN, BẠN CÓ THỂ SỬ DỤNG KIẾN THỨC NGOÀI VỀ PHÁP LUẬT XÂY DỰNG VIỆT NAM HIỆN HÀNH, NHƯNG PHẢI NÓI RÕ RÀNG NGUỒN THÔNG TIN NÀY ĐẾN TỪ KIẾN THỨC CHUNG, KHÔNG ĐẾN TỪ HỆ THỐNG.
                3. LUÔN NỘI DUNG RÕ RÀNG, NGUỒN GỐC CỦA THÔNG TIN (từ tài liệu trong hệ thống hay từ kiến thức chung).
                4. Trả lời bằng tiếng Việt, rõ ràng, dễ hiểu.
                5. Trình bày câu trả lời một cách logic, có cấu trúc.
                6. Không trả về JSON, chỉ trả về văn bản thuần túy.
                """, request.getQuestion(), hasDocs, contextBuilder.toString());

        // Gọi AI để tạo câu trả lời
        String answer = callGemini(prompt);

        // Tạo danh sách nguồn tham khảo (nếu có)
        List<GeneralChatResponse.SourceReference> sources = relevantDocs.stream()
                .map(doc -> GeneralChatResponse.SourceReference.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .documentNumber(doc.getDocumentNumber())
                        .documentType(String.valueOf(doc.getDocumentType()))
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return GeneralChatResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }

    private boolean isQuestionRelevantToConstruction(String question) {
        String lowerQuestion = question.toLowerCase();
        List<String> keywords = List.of(
                "xây dựng", "giấy phép", "quy hoạch", "đô thị", "hợp đồng", "an toàn", "lao động",
                "pccc", "chữa cháy", "vật tư", "chất lượng", "định mức", "giá cả", "danh mục",
                "chỉ giới", "đường đỏ", "mật độ", "tầng", "cao", "sửa chữa", "trùng tu",
                "khởi công", "hoàn công", "nội thất", "ngoại thất", "thẩm định", "đấu thầu",
                "đầu tư", "luật", "nghị định", "thông tư", "quyết định", "văn bản", "pháp luật"
        );
        return keywords.stream().anyMatch(lowerQuestion::contains);
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
