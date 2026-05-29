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

    // delivery.create 토픽에서 문자열 payload를 받아 배송 생성
    // String payload + ObjectMapper 방식으로 DTO 변환
    @KafkaListener(
            topics = CREATE_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:delivery-service}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        String payload = record.value();

        // 메세지 키는 orderId + 미리 null 초기화
        String messageKey = null;

        try {
            // DeliveryRequest DTO 역직렬화 및 검증
            DeliveryRequest request = objectMapper.readValue(payload, DeliveryRequest.class);
            validate(request);

            messageKey = toMessageKey(request.getOrderId());

            DeliveryResponse response = deliveryService.createDelivery(request);
            publish(messageKey, response);

            log.info("배송 생성 이벤트 처리 성공. orderId={}, deliveryId={}",
                    request.getOrderId(),
                    response.getDeliveryId()
            );
        } catch (Exception e) {
            // 실패 시 키에 오프셋을 임시로 넣고 요청 데이터를 그대로 전달
            if (messageKey == null) {
                messageKey = "offset-" + record.offset();
            }
            log.error("배송 생성 이벤트 처리 실패. key={}, offset={}", messageKey, record.offset(), e);
            publishFailed(messageKey, payload);
        }
    }

    private void validate(DeliveryRequest request) {
        // 검증 실패도 실패 토픽으로 보내기 위해 예외 발생
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
