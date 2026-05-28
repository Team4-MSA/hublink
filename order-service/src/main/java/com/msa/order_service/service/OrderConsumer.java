package com.msa.order_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.core_common.error.exception.CustomException;
import com.msa.order_service.dto.req.MakeDeliveryReqDto;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.ProductNPAResDto;
import com.msa.order_service.dto.res.StockResultDto;
import com.msa.order_service.entity.Orders;
import com.msa.order_service.entity.Outbox;
import com.msa.order_service.error.OrderErrorCode;
import com.msa.order_service.repository.OrderJpaRepository;
import com.msa.order_service.repository.OutboxRepository;
import com.msa.order_service.type.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderJpaRepository orderJpaRepository;
    private final OutboxRepository outboxRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock.decrease.success", groupId = "order-group")
    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handleStockSuccess(String jsonMessage, Acknowledgment ack) {
        try {
            StockResultDto result = objectMapper.readValue(jsonMessage, StockResultDto.class);
            UUID orderId = result.getOrderId();

            // 엔티티 PK인 id(UUID)로 깔끔하게 조회합니다.
            Orders order = orderJpaRepository.findById(orderId)
                    .orElseThrow(() -> new CustomException(OrderErrorCode.NOT_EXIST_ORDER));

            // 이미 처리되었지만 offset 반영이 안된 로직은 재시도 방지 (멱등성 가드)
            if (order.getStatus() != Status.PENDING) {
                log.warn("이미 처리 완료된 재고 신호 스킵. 주문 ID: {}", orderId);
                ack.acknowledge();
                return;
            }

            // 고속 조회를 위해 List를 Map으로 전환
            Map<UUID, ProductNPAResDto> productMap = result.getProducts().stream()
                    .collect(Collectors.toMap(ProductNPAResDto::productId, Function.identity()));


            order.getOrderItems().forEach(item -> {
                ProductNPAResDto info = productMap.get(item.getProductId());
                if (info != null) {
                    // 재고 차감 성공 item들 성공처리
                    item.enrichProductDetails(info.name(), info.price(), info.hubId());
                    item.setStatus(Status.COMPLETED);
                }
            });

            // 주문 생성 성공으로 총가격 변경 및 상태 변경
            order.updateTotalPrice();
            order.setStatus(Status.CREATED);

            MakeDeliveryReqDto deliveryReqDto = MakeDeliveryReqDto.from(
                    order,
                    result.getOrdererName(),
                    result.getOrdererEmail(),
                    result.getDeliveryAddress(),
                    result.getReceiverCompanyName()
            );

            // 배송생성 outbox 발행
            orderService.publishMakeDeliveryEvent(deliveryReqDto);

            // 메시지 처리 완료
            ack.acknowledge();

        } catch (Exception e) {
            log.error("재고 성공 처리 예외 발생 - 자동 롤백 및 재배달 유도", e);
            throw new CustomException(OrderErrorCode.FAIL_STOCK);
        }
    }

    @KafkaListener(topics = "stock.decrease.failed", groupId = "order-group")
    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handleStockFailed(String jsonMessage, Acknowledgment ack) {
        try {
            UUID orderId = UUID.fromString(objectMapper.readTree(jsonMessage).get("orderId").asText());
            log.warn("재고 부족으로 인한 주문 취소 공정 가동. 주문 ID: {}", orderId);

            Orders order = orderJpaRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus(Status.FAILED);
                order.getOrderItems().forEach(item -> item.setStatus(Status.FAILED));
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("재고 실패 처리 예외 발생", e);
            throw new CustomException(OrderErrorCode.FAIL_STOCK);
        }
    }

    @KafkaListener(topics = "delivery.succeed", groupId = "order-group")
    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handleDeliverySuccess(String jsonMessage, Acknowledgment ack) {
        try {
            UUID orderId = UUID.fromString(objectMapper.readTree(jsonMessage).get("orderId").asText());
            log.info("주문 최종 확정. 주문 ID: {}", orderId);

            Orders order = orderJpaRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus(Status.COMPLETED);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("배송 완료 처리 예외 발생", e);
            throw new CustomException(OrderErrorCode.FAIL_DELIVERY);
        }
    }

    @KafkaListener(topics = "delivery.failed", groupId = "order-group")
    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handleDeliveryFailed(String jsonMessage, Acknowledgment ack) {
        try {
            UUID orderId = UUID.fromString(objectMapper.readTree(jsonMessage).get("orderId").asText());
            log.error("배송 생성 실패 수령 -> 재고 복구(보상 트랜잭션) 발동. 주문 ID: {}", orderId);

            Orders order = orderJpaRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. ID: " + orderId));

            if (order.getStatus() == Status.FAILED) {
                ack.acknowledge();
                return;
            }

            order.setStatus(Status.FAILED);
            order.getOrderItems().forEach(item -> item.setStatus(Status.FAILED));

            List<OrderMakeReqDto.Items> rollbackItems = order.getOrderItems().stream()
                    .map(item -> new OrderMakeReqDto.Items(item.getProductId(), item.getQuantity()))
                    .toList();

            String rollbackPayload = objectMapper.writeValueAsString(rollbackItems);
            Outbox stockRollbackOutbox = Outbox.builder()
                    .aggregateType("ORDER")
                    .aggregateId(orderId.toString())
                    .topic("stock.increase")
                    .payload(rollbackPayload)
                    .processed(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(stockRollbackOutbox);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("배송 실패 대응 중 시스템 예외 발생", e);
            throw new CustomException(OrderErrorCode.FAIL_DELIVERY);
        }
    }
}