package com.msa.delivery_service.infrastructure.client.hub;

import com.msa.delivery_service.infrastructure.client.hub.dto.HubResponse;
import com.msa.delivery_service.infrastructure.client.hub.dto.HubRouteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "hub-service")
public interface HubClient {

    @GetMapping("/internal/hubs/{hubId}")
    HubResponse getHub(@PathVariable UUID hubId);

    @GetMapping("/api/v1/hub-routes/hub-routes/path")
    List<HubRouteResponse> getRoutes(
            @RequestParam UUID supplierCompanyId,
            @RequestParam UUID receiverCompanyId
    );
}
