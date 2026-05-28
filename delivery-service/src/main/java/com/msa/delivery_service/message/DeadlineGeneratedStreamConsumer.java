package com.msa.delivery_service.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.stream.DeadlineStreamConstants;
import com.msa.delivery_service.service.DeliveryService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineGeneratedStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final DeliveryService deliveryService;

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            process(record);
            acknowledge(record.getId());
        } catch (Exception e) {
            log.error("Failed to process deadline generated event. recordId={}", record.getId(), e);
        }
    }

    void process(MapRecord<String, ?, ?> record) throws Exception {
        Object payloadObj = record.getValue().get("payload");
        if (payloadObj == null) {
            log.warn("Deadline generated event payload is missing. recordId={}", record.getId());
            return;
        }

        DeadlineGeneratedEvent event = objectMapper.readValue(
                String.valueOf(payloadObj),
                DeadlineGeneratedEvent.class
        );
        Set<ConstraintViolation<DeadlineGeneratedEvent>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            log.warn(
                    "Deadline generated event validation failed. recordId={}, violations={}",
                    record.getId(),
                    violations.stream().map(ConstraintViolation::getMessage).toList()
            );
            return;
        }

        deliveryService.updateFinalDepartureDeadline(event);
        log.info(
                "Applied final departure deadline. deliveryId={}, eventId={}",
                event.getDeliveryId(),
                event.getEventId()
        );
    }

    private void acknowledge(RecordId recordId) {
        stringRedisTemplate.opsForStream().acknowledge(
                DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                DeadlineStreamConstants.DELIVERY_SERVICE_GROUP,
                recordId
        );
    }
}
