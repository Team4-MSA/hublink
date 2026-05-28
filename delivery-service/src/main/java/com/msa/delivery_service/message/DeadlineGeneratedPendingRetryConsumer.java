package com.msa.delivery_service.message;

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
            if (pendingMessage.getElapsedTimeSinceLastDelivery().compareTo(PENDING_MIN_IDLE_TIME) < 0) {
                continue;
            }
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
        if (targetRecord == null) {
            acknowledge(streamOps, pendingMessage.getId());
            return;
        }

        try {
            streamConsumer.process(targetRecord);
            acknowledge(streamOps, targetRecord.getId());
            log.info("Retried pending deadline message successfully. recordId={}", targetRecord.getId());
        } catch (Exception e) {
            log.error("Failed to retry pending deadline message. recordId={}", pendingMessage.getId(), e);
        }
    }

    private void moveToDlqAndAcknowledge(
            StreamOperations<String, Object, Object> streamOps,
            PendingMessage pendingMessage
    ) {
        MapRecord<String, Object, Object> targetRecord = findRecord(streamOps, pendingMessage.getId());
        if (targetRecord == null) {
            acknowledge(streamOps, pendingMessage.getId());
            return;
        }

        streamOps.add(
                DeadlineStreamConstants.DEADLINE_GENERATED_DELIVERY_DLQ_STREAM,
                Map.of("payload", String.valueOf(targetRecord.getValue().get("payload")))
        );
        acknowledge(streamOps, targetRecord.getId());
        log.warn(
                "Moved deadline message to DLQ. recordId={}, deliveryCount={}",
                targetRecord.getId(),
                pendingMessage.getTotalDeliveryCount()
        );
    }

    private MapRecord<String, Object, Object> findRecord(
            StreamOperations<String, Object, Object> streamOps,
            RecordId recordId
    ) {
        List<MapRecord<String, Object, Object>> records = streamOps.range(
                DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                Range.closed(recordId.getValue(), recordId.getValue())
        );

        if (records == null || records.isEmpty()) {
            log.warn("Could not find pending message payload in stream. recordId={}", recordId);
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
