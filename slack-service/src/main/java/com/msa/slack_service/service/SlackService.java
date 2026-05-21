package com.msa.slack_service.service;

import com.msa.core_common.response.paging.PageRes;
import com.msa.slack_service.client.SlackClient;
import com.msa.slack_service.dto.SlackMessageResponse;
import com.msa.slack_service.entity.MessageType;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.entity.SlackMessageStatus;
import com.msa.slack_service.exception.SlackErrorCode;
import com.msa.slack_service.repository.SlackMessageRepository;
import com.msa.slack_service.stream.event.DeadlineGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.msa.core_common.error.exception.CustomException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackService {
    private final SlackMessageRepository slackMessageRepository;
    private final SlackMessageService slackMessageService;
    private final SlackClient slackClient;

    // 발송 시한 전송
    public void processDeadlineGenerated(DeadlineGeneratedEvent event) {
        log.info("[1] SlackService 진입: eventId={}, slackId={}, type={}, message={}",
                event.getEventId(), event.getReceiverSlackId(), event.getMessageType(), event.getMessage());

        String idempotencyKey = event.getEventId().toString();

        log.info("[2] findOrCreateMessage 호출 전: idempotencyKey={}", idempotencyKey);

        SlackMessage slackMessage = slackMessageService.findOrCreateMessage(event, idempotencyKey);

        log.info("[3] findOrCreateMessage 호출 후: slackMessageId={}, status={}",
                slackMessage.getSlackMessageId(), slackMessage.getStatus());

        if (slackMessage.getStatus() == SlackMessageStatus.SENT) {
            log.info("[4] 이미 SENT라 전송 생략");
            return;
        }

        log.info("[5] Slack 전송 호출 전");

        sendAndUpdateStatus(slackMessage);

        log.info("[6] Slack 전송 처리 완료");
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
            throw e;
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
