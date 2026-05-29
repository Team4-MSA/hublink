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
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }

            // 미인증 요청 → 클라이언트 실제 IP 기준
            // AWS ALB는 X-Forwarded-For 헤더에 실제 클라이언트 IP를 담아 전달함
            // 형식: "클라이언트IP, ALB내부IP" → 첫 번째 값이 실제 클라이언트 IP
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return Mono.just("ip:" + xff.split(",")[0].trim());
            }

            // X-Forwarded-For 없을 경우 소켓 주소에서 직접 추출 (로컬 개발 환경 등)
            String remoteAddr = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just("ip:" + remoteAddr);
        };
    }
}
