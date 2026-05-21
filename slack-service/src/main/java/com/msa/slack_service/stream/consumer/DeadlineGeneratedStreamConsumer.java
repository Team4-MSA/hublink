package com.msa.slack_service.stream.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.stream.DeadlineStreamConstants;
import com.msa.slack_service.service.SlackService;
import com.msa.slack_service.stream.event.DeadlineGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineGeneratedStreamConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final SlackService slackService;

    @Scheduled(fixedDelay = 3000)
    public void consume() {
        var records = stringRedisTemplate.opsForStream().read(
                Consumer.from(
                        DeadlineStreamConstants.SLACK_SERVICE_GROUP,
                        DeadlineStreamConstants.SLACK_SERVICE_CONSUMER
                ),
                StreamOffset.create(
                        DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                        ReadOffset.lastConsumed()
                )
        );

        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {
            try {
                String payload = String.valueOf(record.getValue().get("payload"));

                DeadlineGeneratedEvent event = objectMapper.readValue(
                        payload,
                        DeadlineGeneratedEvent.class
                );

                log.info("Slack 발송 이벤트 수신: eventId={}, receiverSlackId={}",
                        event.getEventId(),
                        event.getReceiverSlackId()
                );

                slackService.processDeadlineGenerated(event);

                stringRedisTemplate.opsForStream().acknowledge(
                        DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                        DeadlineStreamConstants.SLACK_SERVICE_GROUP,
                        record.getId()
                );

                log.info("Slack 이벤트 ACK 완료: recordId={}", record.getId());

            } catch (Exception e) {
                log.error("Slack Stream 이벤트 처리 실패: recordId={}", record.getId(), e);
            }
        }
    }
}