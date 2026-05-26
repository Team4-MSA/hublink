package com.msa.delivery_service.infrastructure.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.delivery_service.presentation.dto.DeliveryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = RedisStreamEventPublisherTest.TestRedisConfig.class)
@ActiveProfiles("test")
class RedisStreamEventPublisherTest {

    @Autowired
    private RedisStreamEventPublisher redisStreamEventPublisher;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publishEventStream() throws Exception {
        DeliveryRequest.Product product = objectMapper.convertValue(
                Map.of("productName", "keyboard", "quantity", 2),
                DeliveryRequest.Product.class
        );

        DeadlineRequestedEvent event = DeadlineRequestedEvent.builder()
                .eventId(UUID.randomUUID())
                .deliveryId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .ordererName("tester")
                .ordererEmail("tester@hublink.com")
                .orderedAt(LocalDateTime.now())
                .requestMessage("deliver fast")
                .receiverUserId(UUID.randomUUID())
                .receiverSlackId("U123456")
                .products(List.of(product))
                .requestedArrivalAt(LocalDateTime.now())
                .destinationAddress("Seoul")
                .deliveryManagerName("manager")
                .deliveryManagerEmail("manager@hublink.com")
                .routeInfo(List.of())
                .workStartTime("09:00")
                .workEndTime("18:00")
                .build();
        String streamKey = "test:deadline:requested:stream:" + UUID.randomUUID();
        String expectedPayload = objectMapper.writeValueAsString(event);

        try {
            redisStreamEventPublisher.publishAfterCommit(streamKey, event);

            List<MapRecord<String, Object, Object>> records =
                    stringRedisTemplate.opsForStream().range(streamKey, Range.unbounded());

            assertEquals(1, records.size());
            assertEquals(expectedPayload, records.get(0).getValue().get("payload"));
        } finally {
            stringRedisTemplate.delete(streamKey);
        }
    }

    @TestConfiguration
    @Import(RedisStreamEventPublisher.class)
    static class TestRedisConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }

        @Bean
        LettuceConnectionFactory redisConnectionFactory(
                @Value("${spring.data.redis.host}") String host,
                @Value("${spring.data.redis.port}") int port,
                @Value("${spring.data.redis.username}") String username,
                @Value("${spring.data.redis.password:}") String password
        ) {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);

            if (StringUtils.hasText(username)) configuration.setUsername(username);
            if (StringUtils.hasText(password)) configuration.setPassword(RedisPassword.of(password));

            return new LettuceConnectionFactory(configuration);
        }

        @Bean
        StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
            return new StringRedisTemplate(redisConnectionFactory);
        }
    }
}
