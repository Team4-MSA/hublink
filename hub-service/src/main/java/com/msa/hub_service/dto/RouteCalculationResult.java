package com.msa.hub_service.dto;

import com.msa.hub_service.entity.RouteType;

import java.math.BigDecimal;

public record RouteCalculationResult(
        BigDecimal distanceKm,
        int durationMinutes,
        RouteType routeType
) {
}
