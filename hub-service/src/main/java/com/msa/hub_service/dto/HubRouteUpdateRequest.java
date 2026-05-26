package com.msa.hub_service.dto;

import java.math.BigDecimal;

public record HubRouteUpdateRequest(
        BigDecimal estimatedDistanceKm,
        Integer estimatedDurationMin
) {
}
