package com.msa.ai_service.stream.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.ai_service.dto.AiDeadlineResult;
import com.msa.ai_service.entity.AiRequestType;
import com.msa.ai_service.service.AiService;
import com.msa.ai_service.stream.event.DeadlineGeneratedEvent;
import com.msa.ai_service.stream.event.DeadlineNotificationRequestedEvent;
import com.msa.ai_service.stream.publisher.DeadlineGeneratedEventPublisher;
import com.msa.core_common.stream.DeadlineStreamConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineNotificationRequestedStreamConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AiService aiService;
    private final Validator validator;
    private final DeadlineGeneratedEventPublisher deadlineGeneratedEventPublisher;

    @Scheduled(fixedDelay = 3000)
    public void consume() {
        var records = stringRedisTemplate.opsForStream().read(
                Consumer.from(
                        DeadlineStreamConstants.AI_SERVICE_GROUP,
                        DeadlineStreamConstants.AI_SERVICE_CONSUMER
                ),
                StreamReadOptions.empty().count(100),
                StreamOffset.create(
                        DeadlineStreamConstants.DEADLINE_REQUESTED_STREAM,
                        ReadOffset.lastConsumed()
                )
        );

        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {
            try {
                Object payloadObj = record.getValue().get("payload");

                if (payloadObj == null) {
                    log.warn("AI Stream 이벤트 payload 누락으로 ACK 처리: recordId={}", record.getId());

                    stringRedisTemplate.opsForStream().acknowledge(
                            DeadlineStreamConstants.DEADLINE_REQUESTED_STREAM,
                            DeadlineStreamConstants.AI_SERVICE_GROUP,
                            record.getId()
                    );

                    continue;
                }

                DeadlineNotificationRequestedEvent event = objectMapper.readValue(
                        String.valueOf(payloadObj),
                        DeadlineNotificationRequestedEvent.class
                );

                Set<ConstraintViolation<DeadlineNotificationRequestedEvent>> violations =
                        validator.validate(event);

                if (!violations.isEmpty()) {
                    log.warn("AI Stream 이벤트 유효성 검증 실패: recordId={}, violations={}",
                            record.getId(),
                            violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .toList()
                    );

                    stringRedisTemplate.opsForStream().acknowledge(
                            DeadlineStreamConstants.DEADLINE_REQUESTED_STREAM,
                            DeadlineStreamConstants.AI_SERVICE_GROUP,
                            record.getId()
                    );

                    continue;
                }

                log.info("AI 발송 시한 생성 요청 이벤트 수신: eventId={}, deliveryId={}, orderId={}",
                        event.getEventId(),
                        event.getDeliveryId(),
                        event.getOrderId()
                );

                AiDeadlineResult result = aiService.generateDeadline(event);

                DeadlineGeneratedEvent generatedEvent = DeadlineGeneratedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .deliveryId(event.getDeliveryId())
                        .aiMessageId(result.getAiMessageId())
                        .receiverUserId(event.getReceiverUserId())
                        .receiverSlackId(event.getReceiverSlackId())
                        .finalDepartureDeadline(result.getFinalDepartureDeadline())
                        .messageType(AiRequestType.DELIVERY_DEADLINE)
                        .message(result.getMessage())
                        .build();


                deadlineGeneratedEventPublisher.publish(generatedEvent);

                stringRedisTemplate.opsForStream().acknowledge(
                        DeadlineStreamConstants.DEADLINE_REQUESTED_STREAM,
                        DeadlineStreamConstants.AI_SERVICE_GROUP,
                        record.getId()
                );

                log.info("AI 이벤트 ACK 완료: recordId={}", record.getId());

            } catch (Exception e) {
                log.error("AI Stream 이벤트 처리 실패: recordId={}", record.getId(), e);
            }
        }
    }
}