package com.msa.delivery_service.infrastructure.client.user;

import com.msa.delivery_service.infrastructure.client.user.dto.DeliveryManagerResponse;
import com.msa.delivery_service.infrastructure.client.user.dto.HubManagerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/internal")
public interface UserClient {

    @GetMapping("/hubs/{hubId}")
    HubManagerResponse getHubManager(@PathVariable UUID hubId);

    @GetMapping("/hubs/{hubId}/delivery-managers")
    List<DeliveryManagerResponse> getDeliveryManagers(@PathVariable UUID hubId);
}
