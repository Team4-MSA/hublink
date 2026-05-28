package com.msa.delivery_service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.error.exception.CustomException;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisStreamEventPublisher {

    public static final String DEADLINE_REQUESTED_STREAM = "deadline:requested:stream";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publishAfterCommit(String streamKey, Object event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(streamKey, event);
            return;
        }
        // 肄쒕갚 ?⑥닔 ?깅줉
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(streamKey, event);
            }
        });
    }

    private void publish(String streamKey, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            stringRedisTemplate.opsForStream().add(streamKey, Map.of("payload", payload));
        } catch (JsonProcessingException e) {
            throw new CustomException(DeliveryErrorCode.AI_SCHEDULE_EVENT_PUBLISH_FAILED);
        }
    }
}
