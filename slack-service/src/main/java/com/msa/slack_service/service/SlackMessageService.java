package com.msa.slack_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.slack_service.entity.MessageType;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.entity.SlackMessageStatus;
import com.msa.slack_service.exception.SlackErrorCode;
import com.msa.slack_service.repository.SlackMessageRepository;
import com.msa.slack_service.stream.event.DeadlineGeneratedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlackMessageService {
    private final SlackMessageRepository slackMessageRepository;

    // 메세지 생성(있으면 찾아서 반환)
    @Transactional
    public SlackMessage findOrCreateMessage(DeadlineGeneratedEvent event, String idempotencyKey) {
        return slackMessageRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> slackMessageRepository.save(
                        SlackMessage.builder()
                                .receiverUserId(event.getReceiverUserId())
                                .aiMessageId(event.getAiMessageId())
                                .receiverSlackId(event.getReceiverSlackId())
                                .idempotencyKey(idempotencyKey)
                                .messageType(event.getMessageType())
                                .message(event.getMessage())
                                .build()
                ));
    }

    // 발송 성공
    @Transactional
    public void markSent(UUID slackMessageId) {
        SlackMessage slackMessage = slackMessageRepository.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND));

        slackMessage.markSent();
    }

    // 발송 실패
    @Transactional
    public void markFailed(UUID slackMessageId, String reason) {
        SlackMessage slackMessage = slackMessageRepository.findById(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND));

        slackMessage.markFailed(reason);
    }

    public Page<SlackMessage> findAll(
            SlackMessageStatus status,
            MessageType messageType,
            Pageable pageable
    ) {
        return slackMessageRepository.findAllByCondition(status, messageType, pageable);
    }

    public Optional<SlackMessage> findById(UUID slackMessageId) {
        return slackMessageRepository.findById(slackMessageId);
    }

}
