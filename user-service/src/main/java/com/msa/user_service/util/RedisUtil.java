package com.msa.user_service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;
    private final long rtExpiration;

    private static final String RT_PREFIX = "RT:";
    private static final String BL_PREFIX = "BL:";
    private static final String BL_USER_PREFIX = "BL:USER:";

    public RedisUtil(
            RedisTemplate<String, String> redisTemplate,
            @Value("${jwt.refresh-expiration}") long rtExpiration
    ) {
        this.redisTemplate = redisTemplate;
        this.rtExpiration = rtExpiration;
    }

    // RT 저장
    public void saveRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue()
                .set(RT_PREFIX + userId, refreshToken, rtExpiration, TimeUnit.MILLISECONDS);
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

    // 유저 강제 차단 (삭제된 유저의 AT 즉시 무효화)
    // Gateway에서 BL:USER:{userId} 키를 체크하여 차단
    public void blockUser(String userId, long ttl) {
        redisTemplate.opsForValue()
                .set(BL_USER_PREFIX + userId, "deleted", ttl, TimeUnit.MILLISECONDS);
    }

    // RT 삭제 + 유저 차단 한번에 처리
    public void invalidateUser(String userId, long accessTokenTtl) {
        deleteRefreshToken(userId);
        blockUser(userId, accessTokenTtl);
    }
}
