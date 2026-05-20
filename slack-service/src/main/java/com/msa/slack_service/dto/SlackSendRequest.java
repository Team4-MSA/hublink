package com.msa.slack_service.dto;

import com.msa.slack_service.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class SlackSendRequest {
    @NotNull(message = "수신자 유저 ID는 필수입니다.")
    private UUID receiverUserId;

    private UUID aiMessageId;

    @NotBlank(message = "수신자 Slack ID는 필수입니다.")
    private String receiverSlackId;

    @NotBlank(message = "멱등 키는 필수입니다.")
    private String idempotencyKey;

    @NotNull(message = "메시지 타입은 필수입니다.")
    private MessageType messageType;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String message;
}
