package com.msa.api_gateway.exception;

public class RedisUnavailableException extends RuntimeException {
    public RedisUnavailableException() {
        super("Redis 서버에 연결할 수 없습니다.");
    }
}
