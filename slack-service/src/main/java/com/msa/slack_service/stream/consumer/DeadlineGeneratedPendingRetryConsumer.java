package com.msa.slack_service.stream.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.stream.DeadlineStreamConstants;
import com.msa.slack_service.service.SlackService;
import com.msa.slack_service.stream.event.DeadlineGeneratedEvent;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineGeneratedPendingRetryConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final SlackService slackService;
    private final Validator validator;

    @Scheduled(fixedDelay = 300000)
    public void retryPendingMessages() {
        var records = stringRedisTemplate.opsForStream().read(
                Consumer.from(
                        DeadlineStreamConstants.SLACK_SERVICE_GROUP,
                        DeadlineStreamConstants.SLACK_SERVICE_CONSUMER
                ),
                StreamOffset.create(
                        DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                        ReadOffset.from("0")
                )
        );

        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {
            try {
                Object payloadObj = record.getValue().get("payload");

                if (payloadObj == null) {
                    log.warn("Pending Slack 이벤트 payload 누락으로 ACK 처리: recordId={}", record.getId());
                    acknowledge(record);
                    continue;
                }

                DeadlineGeneratedEvent event = objectMapper.readValue(
                        String.valueOf(payloadObj),
                        DeadlineGeneratedEvent.class
                );

                Set<ConstraintViolation<DeadlineGeneratedEvent>> violations = validator.validate(event);
                if (!violations.isEmpty()) {
                    log.warn("Pending Slack 이벤트 유효성 검증 실패로 ACK 처리: recordId={}, violations={}",
                            record.getId(),
                            violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .toList()
                    );
                    acknowledge(record);
                    continue;
                }

                log.info("Pending Slack 이벤트 재처리 시작: recordId={}, eventId={}",
                        record.getId(), event.getEventId());

                slackService.processDeadlineGenerated(event);

                acknowledge(record);

                log.info("Pending Slack 이벤트 ACK 완료: recordId={}", record.getId());

            } catch (Exception e) {
                log.error("Pending Slack 이벤트 재처리 실패로 ACK 처리: recordId={}", record.getId(), e);
                acknowledge(record);
            }
        }
    }

    private void acknowledge(MapRecord<String, Object, Object> record) {
        stringRedisTemplate.opsForStream().acknowledge(
                DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                DeadlineStreamConstants.SLACK_SERVICE_GROUP,
                record.getId()
        );
    }
}