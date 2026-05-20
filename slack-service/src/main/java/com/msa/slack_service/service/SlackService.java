package com.msa.slack_service.service;

import com.msa.slack_service.client.SlackClient;
import com.msa.slack_service.dto.DeadlineGeneratedEvent;
import com.msa.slack_service.dto.SlackMessageResponse;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.exception.SlackErrorCode;
import com.msa.slack_service.repository.SlackMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.msa.core_common.error.exception.CustomException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlackService {
    private final SlackMessageRepository slackMessageRepository;
    private final SlackClient slackClient;

    // 발송 시한 전송
    @Transactional
    public void processDeadlineGenerated(DeadlineGeneratedEvent event) {
        SlackMessage slackMessage = SlackMessage.builder()
                .receiverUserId(event.getReceiverUserId())
                .aiMessageId(event.getAiMessageId())
                .receiverSlackId(event.getReceiverSlackId())
                .idempotencyKey(event.getEventId().toString())
                .messageType(event.getMessageType())
                .message(event.getMessage())
                .build();

        slackMessageRepository.save(slackMessage);

        try {
            slackClient.sendMessage(event.getReceiverSlackId(), event.getMessage());
            slackMessage.markSent();
        } catch (Exception e) {
            slackMessage.markFailed(e.getMessage());
            throw e;
        }
    }

    // 목록 조회
    public List<SlackMessageResponse> getSlackMessages(String role) {
        validateMaster(role);
        return slackMessageRepository.findAll()
                .stream()
                .map(SlackMessageResponse::from)
                .toList();
    }

    // 상세 조회
    public SlackMessageResponse getSlackMessage(String role, UUID slackMessageId) {
        validateMaster(role);
        SlackMessage slackMessage = slackMessageRepository.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND));

        return SlackMessageResponse.from(slackMessage);
    }

    // 재전송
    @Transactional
    public void resendSlackMessage(String role, UUID slackMessageId) {
        validateMaster(role);
        SlackMessage slackMessage = slackMessageRepository.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND));

        // TODO: 실제 Slack API 재전송 연동
        slackMessage.markSent();
    }

    // 권한 검증
    private void validateMaster(String role) {
        if (!"MASTER".equals(role)) {
            throw new CustomException(SlackErrorCode.SLACK_MESSAGE_ACCESS_DENIED);
        }
    }

}
