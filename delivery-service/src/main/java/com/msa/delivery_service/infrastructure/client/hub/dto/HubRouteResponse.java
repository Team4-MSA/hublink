package com.msa.delivery_service.infrastructure.client.hub.dto;

import com.msa.delivery_service.domain.entity.Delivery;
import com.msa.delivery_service.domain.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryLocationType;
import com.msa.delivery_service.domain.enums.DeliveryRouteType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class HubRouteResponse {

    private UUID hubRouteId;
    private Integer sequence;
    private UUID departureHubId;
    private String departureHubName;
    private UUID arrivalHubId;
    private String arrivalHubName;
    private BigDecimal estimatedDistanceKm;
    private Integer estimatedDurationMin;
    private String routeType;

    public static List<DeliveryRouteHistory> toDeliveryRouteHistories(
            Delivery delivery,
            UUID deliveryManagerId,
            List<HubRouteResponse> hubRoutes
    ) {
        List<DeliveryRouteHistory> routeHistories = new ArrayList<>();

        for (HubRouteResponse hubRoute : hubRoutes) {
            routeHistories.add(hubRoute.toDeliveryRouteHistory(delivery, deliveryManagerId));
        }

        routeHistories.add(DeliveryRouteHistory.create(
                delivery,
                deliveryManagerId,
                routeHistories.size() + 1,
                DeliveryRouteType.HUB_TO_COMPANY,
                DeliveryLocationType.HUB,
                delivery.getDestinationHubId(),
                DeliveryLocationType.COMPANY,
                delivery.getReceiverCompanyId(),
                null,
                null
        ));

        return routeHistories;
    }

    public DeliveryRouteHistory toDeliveryRouteHistory(Delivery delivery, UUID deliveryManagerId) {
        return DeliveryRouteHistory.create(
                delivery,
                deliveryManagerId,
                sequence,
                DeliveryRouteType.HUB_TO_HUB,
                DeliveryLocationType.HUB,
                departureHubId,
                DeliveryLocationType.HUB,
                arrivalHubId,
                estimatedDistanceKm,
                estimatedDurationMin
        );
    }
}
