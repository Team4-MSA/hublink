package com.msa.delivery_service.presentation.dto;

import com.msa.delivery_service.domain.enums.DeliveryLocationType;
import com.msa.delivery_service.domain.enums.DeliveryRouteType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryRouteHistoryRequest {

    @NotNull
    private UUID deliveryManagerId;

    @NotNull
    private Integer sequence;

    @NotNull
    private DeliveryRouteType routeType;

    @NotNull
    private DeliveryLocationType departureType;

    @NotNull
    private UUID departureId;

    @NotNull
    private DeliveryLocationType arrivalType;

    @NotNull
    private UUID arrivalId;

    private String locationName;
}
