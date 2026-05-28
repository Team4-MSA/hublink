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
            // MKSTREAM??湲곕낯?곸쑝濡??댁옣?섏뼱 ?덉뼱 ?ㅽ듃由??ㅺ? ?놁뼱??鍮??ㅽ듃由??앹꽦 諛?洹몃９ ?앹꽦
            stringRedisTemplate.opsForStream().createGroup(
                    DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                    ReadOffset.from("0"),
                    DeadlineStreamConstants.DELIVERY_SERVICE_GROUP
            );
            log.info("Redis Stream consumer group???앹꽦?덉뒿?덈떎. stream={}, group={}",
                    DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                    DeadlineStreamConstants.DELIVERY_SERVICE_GROUP
            );
        } catch (RedisSystemException e) {
            // ?대? 洹몃９??議댁옱 ??"BUSYGROUP" ?대씪??臾몄옄?댁쓣 ?ы븿???덉쇅 諛쒖깮 -> 濡쒓렇 異쒕젰 泥섎━
            // "BUSYGROUP" 臾몄옄?댁씠 ?덉쇅 硫붿꽭吏媛 ?꾨땶 cause ?대???議댁옱 -> cause? 理쒖긽???덉쇅 ?꾨? 泥댄겕
            Throwable cause = e.getCause();
            if ((e.getMessage() != null && e.getMessage().contains("BUSYGROUP"))
                    || (cause != null && cause.getMessage() != null && cause.getMessage().contains("BUSYGROUP"))) {
                log.info("Redis Stream consumer group???대? 議댁옱?⑸땲?? stream={}, group={}",
                        DeadlineStreamConstants.DEADLINE_GENERATED_STREAM,
                        DeadlineStreamConstants.DELIVERY_SERVICE_GROUP
                );
                return;
            }
            throw e;
        }
    }
}
