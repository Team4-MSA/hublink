package com.msa.auth;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserServiceClient userServiceClient;
//    private final CircuitBreakerRegistry circuitBreakerRegistry;
//    private final Logger log = LoggerFactory.getLogger(getClass());
//
//    @PostConstruct
//    public void registerEventListener() {
//        circuitBreakerRegistry.circuitBreaker("authService").getEventPublisher()
//                .onStateTransition(event -> log.info("#######CircuitBreaker State Transition: {}", event))
//                .onFailureRateExceeded(event -> log.info("#######CircuitBreaker Failure Rate Exceeded: {}", event))
//                .onCallNotPermitted(event -> log.info("#######CircuitBreaker Call Not Permitted: {}", event))
//                .onError(event -> log.info("#######CircuitBreaker Error: {}", event));
//    }
//
//    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackGetAuthDetails")
//    public String getUserInfo(String userId){
//        return userServiceClient.getUser(userId);
//    }
//
//
//    public String getAuth(String id) {
//        return id+" "+getUserInfo("2");
//    }
//
//    public String fallbackGetProductDetails(String userId, Throwable t) {
//        log.error("####Fallback triggered for userId: {} due to: {}", userId, t.getMessage());
//        return "Fallback";
//    }

}
