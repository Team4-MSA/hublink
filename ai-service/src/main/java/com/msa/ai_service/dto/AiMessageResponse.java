package com.msa.ai_service.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.msa.ai_service.entity.AiMessage;
import com.msa.ai_service.entity.AiMessageStatus;
import com.msa.ai_service.entity.AiRequestType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AiMessageResponse {
    @JsonProperty("ai_message_id")
    private UUID aiMessageId;

    @JsonProperty("request_type")
    private AiRequestType requestType;

    private String prompt;

    @JsonProperty("request_payload")
    private String requestPayload;

    @JsonProperty("final_departure_deadline")
    private LocalDateTime finalDepartureDeadline;

    @JsonProperty("response_content")
    private String responseContent;

    private AiMessageStatus status;

    @JsonProperty("error_message")
    private String errorMessage;

    public static AiMessageResponse from(AiMessage aiMessage) {
        return AiMessageResponse.builder()
                .aiMessageId(aiMessage.getAiMessageId())
                .requestType(aiMessage.getRequestType())
                .prompt(aiMessage.getPrompt())
                .requestPayload(aiMessage.getRequestPayload())
                .finalDepartureDeadline(aiMessage.getFinalDepartureDeadline())
                .responseContent(aiMessage.getResponseContent())
                .status(aiMessage.getStatus())
                .errorMessage(aiMessage.getErrorMessage())
                .build();
    }
}
