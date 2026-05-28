package com.msa.order_service.service;

import com.msa.order_service.entity.Outbox;
import com.msa.order_service.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        List<Outbox> pendingList = outboxRepository.findByProcessedFalse();
        if(pendingList.isEmpty()) return;

        for (Outbox outbox : pendingList) {
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(outbox.getTopic(), outbox.getAggregateId(), outbox.getPayload());

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("카프카 메시지 발행 성공 - Outbox Id : {}, offset: {}",
                            outbox.getId(), result.getRecordMetadata().offset());

                    orderService.markOutboxProcessed(outbox.getId());
                } else {
                    log.error("카프카 메시지 발행 실패 - Outbox Id : {}", outbox.getId(), ex);
                }
            });
        }
    }


}
