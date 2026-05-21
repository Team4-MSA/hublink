package com.msa.delivery_service.presentation.dto;

import com.msa.delivery_service.domain.enums.DeliveryRouteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryRouteStatusUpdateRequest {

    @NotNull
    private DeliveryRouteStatus status;

    private String statusMessage;
    private BigDecimal actualDistanceKm;
    private Integer actualDurationMin;
}
