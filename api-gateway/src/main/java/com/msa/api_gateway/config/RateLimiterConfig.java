package com.msa.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 인증된 요청 → userId 기준
            // JwtAuthFilter가 JWT 검증 성공 후 isAuthenticated attribute를 설정하므로
            // 클라이언트가 X-User-Id를 임의로 조작해도 이 분기에 진입하지 못함
            Boolean isAuthenticated = exchange.getAttributeOrDefault("isAuthenticated", false);
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (Boolean.TRUE.equals(isAuthenticated) && userId != null) {
                return Mono.just(userId);
            }

            // 미인증 요청 → 클라이언트 실제 IP 기준
            // AWS ALB는 클라이언트 실제 IP를 X-Forwarded-For 맨 뒤에 append함
            // 클라이언트가 헤더를 조작해도 ALB가 마지막에 실제 IP를 추가하므로
            // parts[parts.length - 1] (마지막 값)이 신뢰할 수 있는 클라이언트 IP
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                String[] parts = xff.split(",");
                if (parts.length > 0) {
                    String clientIp = parts[parts.length - 1].trim();
                    if (!clientIp.isEmpty()) {
                        return Mono.just("ip:" + clientIp);
                    }
                }
            }

            // X-Forwarded-For 없을 경우 소켓 주소에서 직접 추출 (로컬 개발 환경 등)
            if (exchange.getRequest().getRemoteAddress() != null) {
                return Mono.just("ip:" + exchange.getRequest().getRemoteAddress().getHostString());
            }

            // IP를 특정할 수 없는 경우 Rate Limiting skip
            return Mono.empty();
        };
    }
}
