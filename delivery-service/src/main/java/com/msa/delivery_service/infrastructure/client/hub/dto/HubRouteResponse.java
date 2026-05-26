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
import java.util.Map;
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
            UUID companyDeliveryManagerId,
            List<HubRouteResponse> hubRoutes,
            Map<UUID, UUID> hubDeliveryManagerIds
    ) {
        List<DeliveryRouteHistory> routeHistories = new ArrayList<>();

        for (HubRouteResponse hubRoute : hubRoutes) {
            routeHistories.add(hubRoute.toDeliveryRouteHistory(
                    delivery,
                    hubDeliveryManagerIds.get(hubRoute.getHubRouteId())
            ));
        }

        routeHistories.add(DeliveryRouteHistory.create(
                delivery,
                companyDeliveryManagerId,
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

    public DeliveryRouteHistory toDeliveryRouteHistory(Delivery delivery, UUID hubDeliveryManagerId) {
        return DeliveryRouteHistory.create(
                delivery,
                hubDeliveryManagerId,
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
