package com.msa.hub_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.entity.RouteType;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HubRouteResponse(
        UUID hubRouteId,
        UUID departureHub,
        UUID arrivalHub,
        BigDecimal estimatedDistanceKm,
        Integer estimatedDurationMin,
        RouteType routeType,
        Integer sequence
) {
    public static HubRouteResponse from(HubRouteEntity hubRoute) {
        return new HubRouteResponse(
                hubRoute.getHubRouteId(),
                hubRoute.getDepartureHub().getHubId(),
                hubRoute.getArrivalHub().getHubId(),
                hubRoute.getEstimatedDistanceKm(),
                hubRoute.getEstimatedDurationMin(),
                hubRoute.getRouteType(),
                null
        );
    }
}
