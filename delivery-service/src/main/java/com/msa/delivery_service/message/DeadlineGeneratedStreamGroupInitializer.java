package com.msa.delivery_service.message;

import com.msa.core_common.stream.DeadlineStreamConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineGeneratedStreamGroupInitializer {
    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void createConsumerGroup() {
        try {
            // XGROUP CREATE
            // MKSTREAM이 기본적으로 내장되어 있어 스트림 키가 없어도 빈 스트림 생성 및 그룹 생성
            stringRedisTemplate.opsForStream().createGroup(
                    DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                    ReadOffset.from("0"),
                    DeadlineStreamConstants.DELIVERY_SERVICE_GROUP
            );
            log.info("Redis Stream consumer group을 생성했습니다. stream={}, group={}",
                    DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                    DeadlineStreamConstants.DELIVERY_SERVICE_GROUP
            );
        } catch (RedisSystemException e) {
            // 이미 그룹이 존재 시 "BUSYGROUP" 이라는 문자열을 포함한 예외 발생 -> 로그 출력 처리
            // "BUSYGROUP" 문자열이 예외 메세지가 아닌 cause 내부에 존재 -> cause와 최상위 예외 전부 체크
            Throwable cause = e.getCause();
            if ((e.getMessage() != null && e.getMessage().contains("BUSYGROUP"))
                    || (cause != null && cause.getMessage() != null && cause.getMessage().contains("BUSYGROUP"))) {
                log.info("Redis Stream consumer group이 이미 존재합니다. stream={}, group={}",
                        DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                        DeadlineStreamConstants.DELIVERY_SERVICE_GROUP
                );
                return;
            }
            log.error("Redis Stream consumer group 생성에 실패했습니다. stream={}, group={}",
                    DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                    DeadlineStreamConstants.DELIVERY_SERVICE_GROUP,
                    e
            );
            throw e;
        }
    }
}
