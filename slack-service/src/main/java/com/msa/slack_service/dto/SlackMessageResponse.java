package com.msa.slack_service.dto;

import com.msa.slack_service.entity.MessageType;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.entity.SlackMessageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SlackMessageResponse {
    private UUID slackMessageId;
    private UUID receiverUserId;
    private UUID aiMessageId;
    private String receiverSlackId;
    private String idempotencyKey;
    private MessageType messageType;
    private String message;
    private SlackMessageStatus status;
    private LocalDateTime sentAt;
    private String errorMessage;

    public static SlackMessageResponse from(SlackMessage slackMessage) {
        return SlackMessageResponse.builder()
                .slackMessageId(slackMessage.getSlackMessageId())
                .receiverUserId(slackMessage.getReceiverUserId())
                .aiMessageId(slackMessage.getAiMessageId())
                .receiverSlackId(slackMessage.getReceiverSlackId())
                .idempotencyKey(slackMessage.getIdempotencyKey())
                .messageType(slackMessage.getMessageType())
                .message(slackMessage.getMessage())
                .status(slackMessage.getStatus())
                .sentAt(slackMessage.getSentAt())
                .errorMessage(slackMessage.getErrorMessage())
                .build();
    }
}
