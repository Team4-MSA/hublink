package com.msa.delivery_service.dto;

import com.msa.delivery_service.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryLocationType;
import com.msa.delivery_service.domain.enums.DeliveryRouteStatus;
import com.msa.delivery_service.domain.enums.DeliveryRouteType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class DeliveryRouteHistoryResponse {

    private UUID routeHistoryId;
    private UUID deliveryId;
    private UUID deliveryManagerId;
    private Integer sequence;
    private DeliveryRouteType routeType;
    private DeliveryLocationType departureType;
    private UUID departureId;
    private DeliveryLocationType arrivalType;
    private UUID arrivalId;
    private String locationName;
    private DeliveryRouteStatus status;
    private String statusMessage;
    private BigDecimal estimatedDistanceKm;
    private Integer estimatedDurationMin;
    private BigDecimal actualDistanceKm;
    private Integer actualDurationMin;
    private LocalDateTime processedAt;

    public static DeliveryRouteHistoryResponse from(DeliveryRouteHistory routeHistory) {
        return DeliveryRouteHistoryResponse.builder()
                .routeHistoryId(routeHistory.getDeliveryRouteHistoryId())
                .deliveryId(routeHistory.getDelivery() == null ? null : routeHistory.getDelivery().getDeliveryId())
                .deliveryManagerId(routeHistory.getDeliveryManagerId())
                .sequence(routeHistory.getSequence())
                .routeType(routeHistory.getRouteType())
                .departureType(routeHistory.getDepartureType())
                .departureId(routeHistory.getDepartureId())
                .arrivalType(routeHistory.getArrivalType())
                .arrivalId(routeHistory.getArrivalId())
                .locationName(routeHistory.getLocationName())
                .status(routeHistory.getStatus())
                .statusMessage(routeHistory.getStatusMessage())
                .estimatedDistanceKm(routeHistory.getEstimatedDistanceKm())
                .estimatedDurationMin(routeHistory.getEstimatedDurationMin())
                .actualDistanceKm(routeHistory.getActualDistanceKm())
                .actualDurationMin(routeHistory.getActualDurationMin())
                .processedAt(routeHistory.getProcessedAt())
                .build();
    }
}
