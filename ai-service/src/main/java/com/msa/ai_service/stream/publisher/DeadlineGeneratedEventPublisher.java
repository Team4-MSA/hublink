package com.msa.ai_service.stream.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.ai_service.exception.AiErrorCode;
import com.msa.ai_service.stream.event.DeadlineGeneratedEvent;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.stream.DeadlineStreamConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeadlineGeneratedEventPublisher {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RecordId publish(DeadlineGeneratedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            return stringRedisTemplate.opsForStream().add(
                    DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                    Map.of("payload", payload)
            );

        } catch (Exception e) {
            throw new CustomException(AiErrorCode.AI_EVENT_PUBLISH_FAILED);
        }
    }
}