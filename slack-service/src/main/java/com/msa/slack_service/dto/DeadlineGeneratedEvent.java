package com.msa.slack_service.dto;

import com.msa.slack_service.entity.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeadlineGeneratedEvent {
    private UUID eventId;
    private UUID aiMessageId;
    private UUID receiverUserId;
    private String receiverSlackId;
    private MessageType messageType;
    private String message;
}
