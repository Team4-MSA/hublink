package com.msa.delivery_service.dto;

import com.msa.delivery_service.enums.DeliveryRouteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteStatusUpdateRequest {

    @NotNull
    private DeliveryRouteStatus status;

    private String statusMessage;
    private BigDecimal actualDistanceKm;
    private Integer actualDurationMin;
}
