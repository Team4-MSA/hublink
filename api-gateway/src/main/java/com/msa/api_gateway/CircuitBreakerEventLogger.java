package com.msa.api_gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerEventLogger {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void registerEventListener() {
        register("authCircuitBreaker");
        register("userCircuitBreaker");
        register("orderCircuitBreaker");
    }

    private void register(String name) {
        circuitBreakerRegistry.circuitBreaker(name)
                .getEventPublisher()
                .onStateTransition(event -> log.info("[{}] State Transition: {}", name, event))
                .onFailureRateExceeded(event -> log.info("[{}] Failure Rate Exceeded: {}", name, event))
                .onCallNotPermitted(event -> log.info("[{}] Call Not Permitted: {}", name, event))
                .onError(event -> log.info("[{}] Error: {}", name, event));
    }
}