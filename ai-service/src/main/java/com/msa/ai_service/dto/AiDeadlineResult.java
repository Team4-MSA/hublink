package com.msa.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiDeadlineResult {
    private UUID aiMessageId;
    private LocalDateTime finalDepartureDeadline;
    private String message;

    public static AiDeadlineResult of(
            UUID aiMessageId,
            LocalDateTime finalDepartureDeadline,
            String message
    ) {
        return new AiDeadlineResult(aiMessageId, finalDepartureDeadline, message);
    }
}