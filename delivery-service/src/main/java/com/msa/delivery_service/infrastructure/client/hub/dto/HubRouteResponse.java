package com.msa.delivery_service.infrastructure.client.hub.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class HubRouteResponse {

    private UUID hubRouteId;
    private Integer sequence;
    private UUID departureHubId;
    private UUID arrivalHubId;
    private BigDecimal estimatedDistanceKm;
    private Integer estimatedDurationMin;
    private String routeType;
}
