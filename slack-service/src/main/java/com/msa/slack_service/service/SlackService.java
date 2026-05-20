package com.msa.slack_service.service;

import com.msa.slack_service.client.SlackClient;
import com.msa.slack_service.dto.DeadlineGeneratedEvent;
import com.msa.slack_service.dto.SlackMessageResponse;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.entity.SlackMessageStatus;
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
        // 멱등성 키로 중복 확인
        String idempotencyKey = event.getEventId().toString();
        slackMessageRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresentOrElse(
                        this::processExistingMessage,
                        () -> createAndSendMessage(event, idempotencyKey)
                );
    }

    // 메세지 생성 & 저장
    private void createAndSendMessage(DeadlineGeneratedEvent event, String idempotencyKey) {
        SlackMessage slackMessage = SlackMessage.builder()
                .receiverUserId(event.getReceiverUserId())
                .aiMessageId(event.getAiMessageId())
                .receiverSlackId(event.getReceiverSlackId())
                .idempotencyKey(idempotencyKey)
                .messageType(event.getMessageType())
                .message(event.getMessage())
                .build();

        slackMessageRepository.save(slackMessage);

        sendAndMark(slackMessage);
    }

    // 큐 중복/실패 이벤트 재전송
    private void processExistingMessage(SlackMessage slackMessage) {
        if (slackMessage.getStatus() == SlackMessageStatus.SENT) {
            return;
        }

        sendAndMark(slackMessage);
    }

    // 메세지 전송
    private void sendAndMark(SlackMessage slackMessage) {
        try {
            slackClient.sendMessage(
                    slackMessage.getReceiverSlackId(),
                    slackMessage.getMessage()
            );
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

        sendAndMark(slackMessage);
    }

    // 권한 검증
    private void validateMaster(String role) {
        if (!"MASTER".equals(role)) {
            throw new CustomException(SlackErrorCode.SLACK_MESSAGE_ACCESS_DENIED);
        }
    }

}
