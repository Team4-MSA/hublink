package com.msa.delivery_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.delivery_service.entity.DeliveryOutbox;
import com.msa.delivery_service.repository.DeliveryOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryOutboxService {

    private static final int MAX_RETRY_COUNT = 5;
    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final DeliveryOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueue(String topic, String eventKey, Object payload) {
        /*
            객체 payload -> JSON 문자열로 변환
        */
        String serializedPayload;
        try {
            serializedPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kafka outbox payload 직렬화에 실패했습니다.", e);
        }
        enqueueSerialized(topic, eventKey, serializedPayload);
    }

    @Transactional
    public void enqueueSerialized(String topic, String eventKey, String serializedPayload) {

        if (outboxRepository.existsByTopicAndEventKey(topic, eventKey)) {
            log.debug("Kafka outbox 이벤트가 이미 존재합니다. topic={}, eventKey={}", topic, eventKey);
            return;
        }

        try {
            // 같은 topic + eventKey 조합애 의한 중복 key 제약조건 충돌 제어
            outboxRepository.saveAndFlush(DeliveryOutbox.create(topic, eventKey, serializedPayload));
        } catch (DataIntegrityViolationException e) {
            log.debug("Kafka outbox 이벤트가 동시에 먼저 저장되었습니다. topic={}, eventKey={}", topic, eventKey);
        }
    }

    @Scheduled(fixedDelayString = "${delivery.kafka.outbox.fixed-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        /*
            outbox worker
            서비스 내부 스케줄러가 주기적으로 미발행 row 조회
        */
        List<DeliveryOutbox> outboxes =
                // PENDING, FAILED 상태 중 재시도 횟수 제한 미만인 것만 대상
                // 오래된 것부터 100개씩 처리
                outboxRepository.findTop100ByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(
                        List.of(DeliveryOutbox.Status.PENDING, DeliveryOutbox.Status.FAILED),
                        MAX_RETRY_COUNT
                );

        for (DeliveryOutbox outbox : outboxes) {
            publish(outbox);
        }
    }

    private void publish(DeliveryOutbox outbox) {
        try {
            kafkaTemplate
                    .send(outbox.getTopic(), outbox.getEventKey(), outbox.getPayload())
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS); // 브로커로부터 ack 수신 대기
            // 전송 성공을 확인한 경우만 상태 변경
            outbox.markPublished();
            log.info(
                    "Kafka outbox 이벤트를 발행했습니다. outboxId={}, topic={}, eventKey={}",
                    outbox.getOutboxId(),
                    outbox.getTopic(),
                    outbox.getEventKey()
            );
        } catch (Exception e) {
            // 상태 변경 및 추후 재처리
            outbox.markFailed(e.getMessage());
            log.error(
                    "Kafka outbox 이벤트 발행에 실패했습니다. outboxId={}, topic={}, eventKey={}, retryCount={}",
                    outbox.getOutboxId(),
                    outbox.getTopic(),
                    outbox.getEventKey(),
                    outbox.getRetryCount(),
                    e
            );
        }
    }
}
