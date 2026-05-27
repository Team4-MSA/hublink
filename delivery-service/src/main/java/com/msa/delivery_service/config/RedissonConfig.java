package com.msa.delivery_service.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password}") String password
    ) {
        Config config = new Config();

        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port);

        if (password != null && !password.isBlank()) singleServerConfig.setPassword(password);

        // Watchdog을 설정하여 leaseTime 없이 락을 자동 연장
        // Watchdog Timeout 30초 설정
        config.setLockWatchdogTimeout(30000);

        return Redisson.create(config);
    }
}
