package com.msa.ai_service.stream.event;

import com.msa.ai_service.entity.AiRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
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

    @NotNull(message = "최종 발송 시한은 필수입니다.")
    private LocalDateTime finalDepartureDeadline;

    @NotBlank(message = "메시지 타입은 필수입니다.")
    private AiRequestType messageType;

    @NotBlank(message = "전송 메시지는 필수입니다.")
    private String message;
}