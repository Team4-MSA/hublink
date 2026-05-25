package com.msa.delivery_service.infrastructure.stream;

import com.msa.core_common.stream.DeadlineStreamConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class DeadlineGeneratedStreamListenerConfig {
    private final RedisConnectionFactory redisConnectionFactory;
    private final DeadlineGeneratedStreamConsumer consumer;

    // Long polling 2초 적용
    // 100건씩 처리
    @Bean(destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> deadlineGeneratedListenerContainer() {
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(
                        redisConnectionFactory,
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofSeconds(2))
                                .batchSize(100)
                                .build()
                );

        container.receive(
                Consumer.from(
                        DeadlineStreamConstants.DELIVERY_SERVICE_GROUP,
                        DeadlineStreamConstants.DELIVERY_SERVICE_CONSUMER
                ),
                StreamOffset.create(
                        DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                        ReadOffset.lastConsumed()
                ),
                consumer
        );

        // Bean으로 등록되는 시점에 컨테이너 실행
        container.start();
        return container;
    }
}
