package com.msa.delivery_service.infrastructure.stream;

import com.msa.core_common.stream.DeadlineStreamConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineGeneratedPendingRetryConsumer {
    private static final long MAX_DELIVERY_COUNT = 5L;
    private static final long PENDING_SCAN_COUNT = 100L;
    private static final Duration PENDING_MIN_IDLE_TIME = Duration.ofMinutes(1);

    private final StringRedisTemplate stringRedisTemplate;
    private final DeadlineGeneratedStreamConsumer streamConsumer;

    @Scheduled(fixedDelay = 300_000)
    public void retryPendingMessages() {
        StreamOperations<String, Object, Object> streamOps = stringRedisTemplate.opsForStream();

        // 현재 consumer의 PEL 조회
        PendingMessages pendingMessages = streamOps.pending(
                DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                Consumer.from(
                        DeadlineStreamConstants.DELIVERY_SERVICE_GROUP,
                        DeadlineStreamConstants.DELIVERY_SERVICE_CONSUMER
                ),
                Range.unbounded(),
                PENDING_SCAN_COUNT
        );

        if (pendingMessages.isEmpty()) return;

        for (PendingMessage pendingMessage : pendingMessages) {
            // MIN_IDLE_TIME을 설정하여 메인 스트림이 처리 중인 메세지는 스킵
            if (pendingMessage.getElapsedTimeSinceLastDelivery().compareTo(PENDING_MIN_IDLE_TIME) < 0) {
                continue;
            }
            // 재처리 횟수가 특정 횟수를 넘어가면 DLQ로 적재
            if (pendingMessage.getTotalDeliveryCount() >= MAX_DELIVERY_COUNT) {
                moveToDlqAndAcknowledge(streamOps, pendingMessage);
                continue;
            }

            retryPendingMessage(streamOps, pendingMessage);
        }
    }

    private void retryPendingMessage(
            StreamOperations<String, Object, Object> streamOps,
            PendingMessage pendingMessage
    ) {
        MapRecord<String, Object, Object> targetRecord = findRecord(streamOps, pendingMessage.getId());
        if (targetRecord == null) return;

        try {
            streamConsumer.process(targetRecord);
            acknowledge(streamOps, targetRecord.getId());
            log.info("배송 최종시한 pending 메시지 재처리를 완료했습니다. recordId={}", targetRecord.getId());
        } catch (Exception e) {
            log.error("배송 최종시한 pending 메시지 재처리에 실패했습니다. recordId={}", pendingMessage.getId(), e);
        }
    }

    private void moveToDlqAndAcknowledge(
            StreamOperations<String, Object, Object> streamOps,
            PendingMessage pendingMessage
    ) {
        MapRecord<String, Object, Object> targetRecord = findRecord(streamOps, pendingMessage.getId());
        if (targetRecord == null) return;

        streamOps.add(
                DeadlineStreamConstants.DEADLINE_GENERATED_DELIVERY_DLQ_STREAM,
                Map.of("payload", String.valueOf(targetRecord.getValue().get("payload")))
        );
        acknowledge(streamOps, targetRecord.getId());
        log.warn("배송 최종시한 메시지를 DLQ로 이동했습니다. recordId={}, deliveryCount={}",
                targetRecord.getId(),
                pendingMessage.getTotalDeliveryCount()
        );
    }

    private MapRecord<String, Object, Object> findRecord(
            StreamOperations<String, Object, Object> streamOps,
            RecordId recordId
    ) {
        // XRANGE로 메인 스트림에서 payload 조회
        List<MapRecord<String, Object, Object>> records = streamOps.range(
                DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                Range.closed(recordId.getValue(), recordId.getValue())
        );

        if (records == null || records.isEmpty()) {
            log.warn("pending 메시지의 stream 본문을 찾을 수 없습니다. recordId={}", recordId);
            return null;
        }

        return records.get(0);
    }

    private void acknowledge(StreamOperations<String, Object, Object> streamOps, RecordId recordId) {
        streamOps.acknowledge(
                DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                DeadlineStreamConstants.DELIVERY_SERVICE_GROUP,
                recordId
        );
    }
}
