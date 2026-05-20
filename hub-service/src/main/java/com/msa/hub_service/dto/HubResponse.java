package com.msa.hub_service.dto;

import com.msa.hub_service.entity.HubEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record HubResponse (
        UUID hubId,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
){
    public static HubResponse from(HubEntity hub){
        return new HubResponse(
                hub.getHubId(),
                hub.getName(),
                hub.getAddress(),
                hub.getLatitude(),
                hub.getLongitude()
        );
    }
}
