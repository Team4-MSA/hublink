package com.msa.delivery_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Configuration
public class FeignConfig {

    private static final List<String> FORWARD_HEADERS = List.of(
            "X-User-Id",
            "X-User-Role"
//            "X-Request-Id"
    );

    @Bean
    // 요청 헤더를 가로채서 내부 Feign 호출에도 이어서 전달
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            FORWARD_HEADERS.forEach(headerName -> {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null && !headerValue.isBlank()) {
                    template.header(headerName, headerValue);
                }
            });
        };
    }
}
