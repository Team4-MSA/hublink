package com.msa.user_service.client;

import com.msa.user_service.dto.HubExistsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-service")
public interface HubClient {

    @GetMapping("/internal/hubs/{hubId}/exists")
    HubExistsResponse checkHubExists(@PathVariable UUID hubId);
}
