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
            // AWS ALB 환경: X-Forwarded-For 형식 = "클라이언트IP, ALB내부IP"
            // 클라이언트가 헤더를 조작해도 ALB가 실제 IP를 맨 뒤에 append하므로
            // 마지막에서 두 번째 값(ALB 바로 앞 홉)이 신뢰할 수 있는 클라이언트 IP
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                String[] parts = xff.split(",");
                String clientIp = parts.length >= 2
                        ? parts[parts.length - 2].trim()
                        : parts[0].trim();
                return Mono.just("ip:" + clientIp);
            }

            // X-Forwarded-For 없을 경우 소켓 주소에서 직접 추출 (로컬 개발 환경 등)
            // getHostString() 사용 — getAddress()는 unresolved 주소일 때 null 반환 가능
            String remoteAddr = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getHostString()
                    : "unknown";
            return Mono.just("ip:" + remoteAddr);
        };
    }
}
