package com.msa.slack_service.service;

import com.msa.core_common.auth.UserRole;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.slack_service.dto.SlackMessageResponse;
import com.msa.slack_service.entity.MessageType;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.entity.SlackMessageStatus;
import com.msa.slack_service.exception.SlackErrorCode;
import com.msa.slack_service.repository.SlackMessageRepository;
import com.msa.slack_service.stream.event.DeadlineGeneratedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlackMessageServiceTest {
    @InjectMocks
    private SlackMessageService slackMessageService;

    @Mock
    private SlackMessageRepository slackMessageRepository;

    private UUID slackMessageId;
    private String idempotencyKey;
    private DeadlineGeneratedEvent event;
    private SlackMessage slackMessage;

    @BeforeEach
    void setUp() {
        slackMessageId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID().toString();

        event = new DeadlineGeneratedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "U123456",
                MessageType.DELIVERY_DEADLINE,
                "메시지 본문",
                LocalDateTime.of(2026, 5, 30, 15, 0)
        );

        slackMessage = SlackMessage.builder()
                .slackMessageId(slackMessageId)
                .receiverUserId(event.getReceiverUserId())
                .aiMessageId(event.getAiMessageId())
                .receiverSlackId(event.getReceiverSlackId())
                .idempotencyKey(idempotencyKey)
                .messageType(event.getMessageType())
                .message(event.getMessage())
                .status(SlackMessageStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("findOrCreateMessage - 기존 멱등 키 메시지 반환")
    void findOrCreateMessage_ReturnExisting() {
        when(slackMessageRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(slackMessage));

        SlackMessage result = slackMessageService.findOrCreateMessage(event, idempotencyKey);

        assertThat(result.getSlackMessageId()).isEqualTo(slackMessageId);
        verify(slackMessageRepository, never()).save(any(SlackMessage.class));
    }

    @Test
    @DisplayName("findOrCreateMessage - 멱등 키가 없으면 신규 저장")
    void findOrCreateMessage_CreateNew() {
        when(slackMessageRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(slackMessageRepository.save(any(SlackMessage.class))).thenReturn(slackMessage);

        SlackMessage result = slackMessageService.findOrCreateMessage(event, idempotencyKey);

        assertThat(result).isNotNull();
        verify(slackMessageRepository, times(1)).save(any(SlackMessage.class));
    }

    @Test
    @DisplayName("markSent - 상태를 SENT로 변경")
    void markSent_Success() {
        when(slackMessageRepository.findById(slackMessageId)).thenReturn(Optional.of(slackMessage));

        slackMessageService.markSent(slackMessageId);

        assertThat(slackMessage.getStatus()).isEqualTo(SlackMessageStatus.SENT);
        assertThat(slackMessage.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("markFailed - 상태를 FAILED로 변경")
    void markFailed_Success() {
        when(slackMessageRepository.findById(slackMessageId)).thenReturn(Optional.of(slackMessage));

        slackMessageService.markFailed(slackMessageId, "전송 실패");

        assertThat(slackMessage.getStatus()).isEqualTo(SlackMessageStatus.FAILED);
        assertThat(slackMessage.getErrorMessage()).isEqualTo("전송 실패");
    }

    @Test
    @DisplayName("getSlackMessages - MASTER 권한으로 목록 조회 성공")
    void getSlackMessages_Success_Master() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SlackMessage> page = new PageImpl<>(List.of(slackMessage), pageable, 1);

        when(slackMessageRepository.findAllByCondition(
                eq(SlackMessageStatus.PENDING),
                eq(MessageType.DELIVERY_DEADLINE),
                eq(pageable)
        )).thenReturn(page);

        PageRes<SlackMessageResponse> result = slackMessageService.getSlackMessages(
                UserRole.MASTER.name(),
                SlackMessageStatus.PENDING,
                MessageType.DELIVERY_DEADLINE,
                pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlackMessageId()).isEqualTo(slackMessageId);
    }

    @Test
    @DisplayName("getSlackMessage - MASTER 권한으로 단건 조회 성공")
    void getSlackMessage_Success_Master() {
        when(slackMessageRepository.findById(slackMessageId)).thenReturn(Optional.of(slackMessage));

        SlackMessageResponse result = slackMessageService.getSlackMessage("MASTER", slackMessageId);

        assertThat(result.getSlackMessageId()).isEqualTo(slackMessageId);
    }

    @Test
    @DisplayName("getSlackMessages - MASTER 권한이 아니면 접근 거부")
    void getSlackMessages_Fail_NotMaster() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> slackMessageService.getSlackMessages(
                        "HUB_MANAGER",
                        SlackMessageStatus.PENDING,
                        MessageType.DELIVERY_DEADLINE,
                        PageRequest.of(0, 10)
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(SlackErrorCode.SLACK_MESSAGE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("getEntity - 존재하지 않는 메시지 조회 시 예외 발생")
    void getEntity_Fail_NotFound() {
        when(slackMessageRepository.findById(slackMessageId)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> slackMessageService.getEntity(slackMessageId)
        );

        assertThat(exception.getErrorCode()).isEqualTo(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND);
    }
}
