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
//                        .uri("lb://auth-service")
//                )
//                .route("user-service", r -> r
//                        .path("/users/**")
//                        .uri("lb://user-service")
//                )
//                .route("order-service", r -> r
//                        .path("/orders/**")
//                        .uri("lb://order-service")
//                )
//                .build();
//    }
//}
