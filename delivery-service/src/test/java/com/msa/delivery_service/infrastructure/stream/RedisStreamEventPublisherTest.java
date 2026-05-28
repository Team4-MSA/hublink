package com.msa.delivery_service.infrastructure.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisStreamEventPublisherTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    private ObjectMapper objectMapper;
    private RedisStreamEventPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        publisher = new RedisStreamEventPublisher(stringRedisTemplate, objectMapper);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("이벤트 발행: commit 이후 payload 저장 검증")
    void publishAfterCommit() throws Exception {
        // given
        String streamKey = "deadline:requested:stream";
        TestEvent event = new TestEvent("delivery-1");
        TransactionSynchronizationManager.initSynchronization();

        // when

        // commit 이전 지연 검증
        publisher.publishAfterCommit(streamKey, event);
        verify(stringRedisTemplate, never()).opsForStream();

        when(stringRedisTemplate.opsForStream()).thenReturn(streamOperations);
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        // then

        // payload 저장 검증
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(streamOperations).add(org.mockito.ArgumentMatchers.eq(streamKey), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).containsEntry("payload", objectMapper.writeValueAsString(event));
    }

    private record TestEvent(String deliveryId) {
    }
}
