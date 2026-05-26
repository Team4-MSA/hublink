package com.msa.order_service.feign.circuit;

import com.msa.order_service.dto.res.UsernameResDto;
import com.msa.order_service.feign.UserFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCircuitService {

    private final UserFeignClient userFeignClient;

    @CircuitBreaker(name = "userRead", fallbackMethod = "getUserNamesFallback")
    public List<UsernameResDto> getUserNames(List<UUID> userIds) {
        return userFeignClient.getUserNames(userIds);
    }

    public List<UsernameResDto> getUserNamesFallback(List<UUID> userIds, Throwable t) {
        log.error("[User Service] 가 응답하지 않아 Fallback 로직이 실행됩니다. 원인: {}", t.getMessage());

        // 유저 서버가 죽어도 주문 목록은 나오게 "알 수 없는 유저"로 채워서 리턴
        return userIds.stream()
                .map(id -> new UsernameResDto(id, "알 수 없는 유저(서버 점검 중)", null, null))
                .toList();
    }

}
