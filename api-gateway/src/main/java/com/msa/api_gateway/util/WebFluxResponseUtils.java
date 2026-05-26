package com.msa.api_gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.api_gateway.response.GatewayResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class WebFluxResponseUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private WebFluxResponseUtils() {}

    public static Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GatewayResponse body = GatewayResponse.failure(status, message);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            String code = status.name();
            bytes = ("{\"status\":" + status.value() + ",\"message\":\"" + code + "\",\"errors\":{\"errorClassName\":\"" + code + "\",\"message\":\"" + message + "\"}}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
