package com.msa.slack_service.stream.event;

import com.msa.slack_service.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineGeneratedEvent {
    @NotNull(message = "이벤트 ID는 필수입니다.")
    private UUID eventId;

    @NotNull(message = "AI 메시지 ID는 필수입니다.")
    private UUID aiMessageId;

    @NotNull(message = "수신자 사용자 ID는 필수입니다.")
    private UUID receiverUserId;

    @NotBlank(message = "수신자 Slack ID는 필수입니다.")
    private String receiverSlackId;

    @NotNull(message = "메시지 타입은 필수입니다.")
    private MessageType messageType;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String message;

    @NotNull(message = "최종 발송 시한은 필수입니다.")
    private LocalDateTime finalDepartureDeadline;
}