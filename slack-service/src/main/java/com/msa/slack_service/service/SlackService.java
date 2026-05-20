package com.msa.slack_service.service;

import com.msa.core_common.response.paging.PageRes;
import com.msa.slack_service.client.SlackClient;
import com.msa.slack_service.dto.DeadlineGeneratedEvent;
import com.msa.slack_service.dto.SlackMessageResponse;
import com.msa.slack_service.entity.MessageType;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.entity.SlackMessageStatus;
import com.msa.slack_service.exception.SlackErrorCode;
import com.msa.slack_service.repository.SlackMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.msa.core_common.error.exception.CustomException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlackService {
    private final SlackMessageRepository slackMessageRepository;
    private final SlackMessageService slackMessageService;
    private final SlackClient slackClient;

    // 발송 시한 전송
    public void processDeadlineGenerated(DeadlineGeneratedEvent event) {
        // 멱등성 키로 중복 확인
        String idempotencyKey = event.getEventId().toString();
        SlackMessage slackMessage = slackMessageService.findOrCreateMessage(event, idempotencyKey);

        // 이미 보냈으면 전송 x
        if (slackMessage.getStatus() == SlackMessageStatus.SENT) {
            return;
        }

        sendAndUpdateStatus(slackMessage);
    }


    // 메세지 전송
    private void sendAndUpdateStatus(SlackMessage slackMessage) {
        try {
            slackClient.sendMessage(
                    slackMessage.getReceiverSlackId(),
                    slackMessage.getMessage()
            );
            slackMessageService.markSent(slackMessage.getSlackMessageId());
        } catch (Exception e) {
            slackMessageService.markFailed(slackMessage.getSlackMessageId(), e.getMessage());
        }
    }

    // 목록 조회
    public PageRes<SlackMessageResponse> getSlackMessages(
            String role,
            SlackMessageStatus status,
            MessageType messageType,
            Pageable pageable
    ) {
        validateMaster(role);
        Page<SlackMessageResponse> page = slackMessageService
                .findAll(status, messageType, pageable)
                .map(SlackMessageResponse::from);

        return new PageRes<>(page);
    }

    // 상세 조회
    public SlackMessageResponse getSlackMessage(String role, UUID slackMessageId) {
        validateMaster(role);
        SlackMessage slackMessage = slackMessageService.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND));

        return SlackMessageResponse.from(slackMessage);
    }

    // 재전송
    @Transactional
    public void resendSlackMessage(String role, UUID slackMessageId) {
        validateMaster(role);
        SlackMessage slackMessage = slackMessageService.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND));

        sendAndUpdateStatus(slackMessage);
    }

    // 권한 검증
    private void validateMaster(String role) {
        if (!"MASTER".equals(role)) {
            throw new CustomException(SlackErrorCode.SLACK_MESSAGE_ACCESS_DENIED);
        }
    }

}
