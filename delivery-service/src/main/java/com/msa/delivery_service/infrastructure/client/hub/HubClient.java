package com.msa.delivery_service.infrastructure.client.hub;

import com.msa.delivery_service.infrastructure.client.hub.dto.HubResponse;
import com.msa.delivery_service.infrastructure.client.hub.dto.HubRouteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hub-service", path = "/internal")
public interface HubClient {

    @GetMapping("/hubs/{hubId}")
    HubResponse getHub(@PathVariable UUID hubId);

    @GetMapping("/routes")
    List<HubRouteResponse> getRoutes(
            @RequestParam UUID departureHubId,
            @RequestParam UUID destinationHubId
    );
}
