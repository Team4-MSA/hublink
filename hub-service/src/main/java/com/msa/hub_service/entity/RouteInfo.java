package com.msa.hub_service.entity;

import java.math.BigDecimal;

public record RouteInfo(
        BigDecimal distanceKm,
        int durationMin,
        RouteType routeType
) {
}
