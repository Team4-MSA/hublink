package com.msa.company_service.client;

import com.msa.company_service.dto.CoordinateDto;
import com.msa.core_common.response.GlobalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "hub-service", fallbackFactory = HubClientFallbackFactory.class)
public interface HubClient {
    @GetMapping("/internal/hubs/{hubId}/exists")
    Boolean getHubExist(@PathVariable UUID hubId);

    @GetMapping("/internal/hubs/coordinates")
    CoordinateDto getCoordinates(@RequestParam("address") String address);
}
