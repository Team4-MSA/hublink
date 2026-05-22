package com.msa.ai_service.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.ai_service.dto.AiDeadlineResult;
import com.msa.ai_service.exception.AiErrorCode;
import com.msa.core_common.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiResponseParser {
    private final ObjectMapper objectMapper;

    public AiDeadlineResult parseDeadlineResponse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalStateException("AI 응답이 비어 있습니다.");
        }

        String cleanedText = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim();

        try {
            return objectMapper.readValue(cleanedText, AiDeadlineResult.class);
        } catch (Exception e) {
            throw new CustomException(AiErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }
}