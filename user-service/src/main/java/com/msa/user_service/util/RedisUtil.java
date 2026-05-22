package com.msa.user_service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RT_PREFIX = "RT:";
    private static final String BL_PREFIX = "BL:";
    private static final long RT_EXPIRATION = 1209600000L;

    // RT 저장
    public void saveRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(RT_PREFIX + userId, refreshToken, RT_EXPIRATION, TimeUnit.MILLISECONDS);
    }

    // RT 조회
    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(RT_PREFIX + userId);
    }

    // RT 삭제 (로그아웃)
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(RT_PREFIX + userId);
    }

    // RT 존재 여부
    public boolean hasRefreshToken(String userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RT_PREFIX + userId));
    }

    // 블랙리스트 등록 (로그아웃된 AT)
    public void addBlacklist(String accessToken, long expiration) {
        redisTemplate.opsForValue()
                .set(BL_PREFIX + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트 확인
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BL_PREFIX + accessToken));
    }
}
