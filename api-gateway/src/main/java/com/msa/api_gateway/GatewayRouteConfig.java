//package com.msa.api_gateway;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class GatewayRouteConfig {
//
//    @Bean
//    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("auth-service", p -> p
//                        .path("/auth/**")
//                        .filters(f -> f.circuitBreaker(config -> config
//                                .setName("authCircuitBreaker")
//                                .setFallbackUri("forward:/fallback")))
//                        .uri("lb://auth-service")
//                )
//                .route("user-service", r -> r
//                        .path("/users/**")
//                        .filters(f -> f.circuitBreaker(config -> config
//                                .setName("userCircuitBreaker")
//                                .setFallbackUri("forward:/fallback")))
//                        .uri("lb://user-service")
//                )
//                .route("order-service", r -> r
//                        .path("/orders/**")
//                        .filters(f -> f.circuitBreaker(config -> config
//                                .setName("orderCircuitBreaker")
//                                .setFallbackUri("forward:/fallback")))
//                        .uri("lb://order-service")
//                )
//                .build();
//    }
//}
