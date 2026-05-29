package com.msa.ai_service.service;

import com.msa.ai_service.dto.AiMessageResponse;
import com.msa.ai_service.entity.AiMessage;
import com.msa.ai_service.entity.AiMessageStatus;
import com.msa.ai_service.entity.AiRequestType;
import com.msa.ai_service.exception.AiErrorCode;
import com.msa.ai_service.repository.AiMessageRepository;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMessageServiceTest {
    @InjectMocks
    private AiMessageService aiMessageService;

    @Mock
    private AiMessageRepository aiMessageRepository;

    private UUID aiMessageId;
    private UUID deliveryId;
    private AiMessage aiMessage;

    @BeforeEach
    void setUp() {
        aiMessageId = UUID.randomUUID();
        deliveryId = UUID.randomUUID();

        aiMessage = AiMessage.builder()
                .aiMessageId(aiMessageId)
                .deliveryId(deliveryId)
                .requestType(AiRequestType.DELIVERY_DEADLINE)
                .prompt("프롬프트")
                .requestPayload("{payload}")
                .status(AiMessageStatus.PENDING)
                .errorMessage("")
                .build();
    }

    @Test
    @DisplayName("saveMessage - 요청 이력 저장 성공")
    void saveMessage_Success() {
        when(aiMessageRepository.save(any(AiMessage.class))).thenReturn(aiMessage);

        AiMessage result = aiMessageService.saveMessage(
                deliveryId,
                AiRequestType.DELIVERY_DEADLINE,
                "프롬프트",
                "{payload}"
        );

        assertThat(result).isNotNull();
        assertThat(result.getAiMessageId()).isEqualTo(aiMessageId);
        verify(aiMessageRepository, times(1)).save(any(AiMessage.class));
    }

    @Test
    @DisplayName("markCompleted - 상태를 SUCCESS로 변경")
    void markCompleted_Success() {
        LocalDateTime deadline = LocalDateTime.of(2026, 5, 30, 15, 0);
        String response = "최종 발송 시한 안내";

        when(aiMessageRepository.findByAiMessageIdAndDeletedAtIsNull(aiMessageId))
                .thenReturn(Optional.of(aiMessage));

        aiMessageService.markCompleted(aiMessageId, deadline, response);

        assertThat(aiMessage.getStatus()).isEqualTo(AiMessageStatus.SUCCESS);
        assertThat(aiMessage.getFinalDepartureDeadline()).isEqualTo(deadline);
        assertThat(aiMessage.getResponseContent()).isEqualTo(response);
    }

    @Test
    @DisplayName("markFailed - 상태를 FAILED로 변경")
    void markFailed_Success() {
        when(aiMessageRepository.findByAiMessageIdAndDeletedAtIsNull(aiMessageId))
                .thenReturn(Optional.of(aiMessage));

        aiMessageService.markFailed(aiMessageId, "AI 응답 파싱 실패");

        assertThat(aiMessage.getStatus()).isEqualTo(AiMessageStatus.FAILED);
        assertThat(aiMessage.getErrorMessage()).isEqualTo("AI 응답 파싱 실패");
    }

    @Test
    @DisplayName("getAiMessages - MASTER 권한으로 목록 조회 성공")
    void getAiMessages_Success_Master() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AiMessage> page = new PageImpl<>(List.of(aiMessage), pageable, 1);

        when(aiMessageRepository.findAllByCondition(
                eq(AiRequestType.DELIVERY_DEADLINE),
                eq(AiMessageStatus.PENDING),
                eq(pageable)
        )).thenReturn(page);

        PageRes<AiMessageResponse> result = aiMessageService.getAiMessages(
                "MASTER",
                AiRequestType.DELIVERY_DEADLINE,
                AiMessageStatus.PENDING,
                pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAiMessageId()).isEqualTo(aiMessageId);
    }

    @Test
    @DisplayName("getAiMessage - MASTER 권한으로 단건 조회 성공")
    void getAiMessage_Success_Master() {
        when(aiMessageRepository.findByAiMessageIdAndDeletedAtIsNull(aiMessageId))
                .thenReturn(Optional.of(aiMessage));

        AiMessageResponse result = aiMessageService.getAiMessage("MASTER", aiMessageId);

        assertThat(result.getAiMessageId()).isEqualTo(aiMessageId);
        assertThat(result.getRequestType()).isEqualTo(AiRequestType.DELIVERY_DEADLINE);
    }

    @Test
    @DisplayName("getAiMessages - MASTER 권한이 아니면 접근 거부")
    void getAiMessages_Fail_NotMaster() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> aiMessageService.getAiMessages(
                        "HUB_MANAGER",
                        AiRequestType.DELIVERY_DEADLINE,
                        AiMessageStatus.PENDING,
                        PageRequest.of(0, 10)
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(AiErrorCode.AI_MESSAGE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("getAiMessageEntity - 존재하지 않는 메시지 조회 시 예외 발생")
    void getAiMessageEntity_Fail_NotFound() {
        when(aiMessageRepository.findByAiMessageIdAndDeletedAtIsNull(aiMessageId))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> aiMessageService.getAiMessageEntity(aiMessageId)
        );

        assertThat(exception.getErrorCode()).isEqualTo(AiErrorCode.AI_MESSAGE_NOT_FOUND);
    }
}
