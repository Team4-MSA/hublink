package com.msa.delivery_service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.delivery_service.service.DeliveryService;
import com.msa.delivery_service.dto.DeliveryRequest;
import com.msa.delivery_service.dto.DeliveryResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCreateKafkaConsumer {

    private static final String CREATE_TOPIC = "delivery.create";
    private static final String CREATE_SUCCEED_TOPIC = "delivery.create.succeed";
    private static final String CREATE_FAILED_TOPIC = "delivery.create.failed";

    private final DeliveryService deliveryService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @KafkaListener(
            topics = CREATE_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:delivery-service}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String payload = record.value();
        String messageKey = null;

        try {
            DeliveryRequest request = objectMapper.readValue(payload, DeliveryRequest.class);
            validate(request);

            messageKey = toMessageKey(request.getOrderId());

            DeliveryResponse response = deliveryService.createDelivery(request);
            publish(messageKey, response);

            log.info(
                    "Processed delivery.create event successfully. orderId={}, deliveryId={}",
                    request.getOrderId(),
                    response.getDeliveryId()
            );
        } catch (Exception e) {
            if (messageKey == null) {
                messageKey = "offset-" + record.offset();
            }
            log.error(
                    "Failed to process delivery.create event. key={}, offset={}",
                    messageKey,
                    record.offset(),
                    e
            );
            publishFailed(messageKey, payload);
        }
    }

    private void validate(DeliveryRequest request) {
        Set<ConstraintViolation<DeliveryRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void publish(String key, Object payload) throws JsonProcessingException {
        kafkaTemplate.send(CREATE_SUCCEED_TOPIC, key, objectMapper.writeValueAsString(payload));
    }

    private void publishFailed(String key, String originalPayload) {
        kafkaTemplate.send(CREATE_FAILED_TOPIC, key, originalPayload);
    }

    private String toMessageKey(UUID orderId) {
        return orderId == null ? null : orderId.toString();
    }
}
