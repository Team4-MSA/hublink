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
        // 카프카 메시지에서 원본 payload 추출
        String payload = record.value();
        // 성공/실패 이벤트 전송 시 사용할 메시지 키
        String messageKey = null;

        try {
            // payload 역직렬화 후 유효성 검증
            DeliveryRequest request = objectMapper.readValue(payload, DeliveryRequest.class);
            validate(request);

            // 같은 주문은 같은 키를 쓰도록 orderId를 메시지 키로 사용
            messageKey = toMessageKey(request.getOrderId());

            // 배송 생성 후 성공 이벤트 발행
            DeliveryResponse response = deliveryService.createDelivery(request);
            publish(messageKey, response);

            log.info(
                    "delivery.create 이벤트를 정상 처리했습니다. orderId={}, deliveryId={}",
                    request.getOrderId(),
                    response.getDeliveryId()
            );
        } catch (Exception e) {
            // 역직렬화 단계에서 실패한 경우를 대비해 offset 기반 임시 키 사용
            if (messageKey == null) {
                messageKey = "offset-" + record.offset();
            }
            log.error(
                    "delivery.create 이벤트 처리에 실패했습니다. key={}, offset={}",
                    messageKey,
                    record.offset(),
                    e
            );
            // 원본 payload를 실패 토픽으로 그대로 전송
            publishFailed(messageKey, payload);
        }
    }

    private void validate(DeliveryRequest request) {
        // Bean Validation으로 필수값 누락 여부 검사
        Set<ConstraintViolation<DeliveryRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void publish(String key, Object payload) throws JsonProcessingException {
        // 성공 결과를 JSON으로 직렬화해 success 토픽으로 전송
        kafkaTemplate.send(CREATE_SUCCEED_TOPIC, key, objectMapper.writeValueAsString(payload));
    }

    private void publishFailed(String key, String originalPayload) {
        // 실패 시 원본 payload를 그대로 failed 토픽으로 전송
        kafkaTemplate.send(CREATE_FAILED_TOPIC, key, originalPayload);
    }

    private String toMessageKey(UUID orderId) {
        // orderId가 있으면 메시지 키로 사용
        return orderId == null ? null : orderId.toString();
    }
}
