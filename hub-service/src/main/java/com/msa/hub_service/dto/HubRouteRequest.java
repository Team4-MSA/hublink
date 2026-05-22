package com.msa.hub_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HubRouteRequest(
        @NotNull(message = "출발 허브 ID는 필수입니다")
        UUID departureHub,

        @NotNull(message = "도착 허브 ID는 필수입니다.")
        UUID arrivalHub
) {
}
