package com.msa.delivery_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.msa.delivery_service.entity.Delivery;
import com.msa.delivery_service.entity.DeliveryRouteHistory;
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

    @JsonAlias("departureHub")
    private UUID departureHubId;

    private String departureHubName;

    @JsonAlias("arrivalHub")
    private UUID arrivalHubId;

    private String arrivalHubName;
    private UUID arrivalCompanyId;
    private String arrivalCompanyName;
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

        for (int i = 0; i < hubRoutes.size(); i++) {
            HubRouteResponse hubRoute = hubRoutes.get(i);
            boolean companyRoute = i == hubRoutes.size() - 1;

            routeHistories.add(hubRoute.toDeliveryRouteHistory(
                    delivery,
                    companyDeliveryManagerId,
                    hubDeliveryManagerIds.get(hubRoute.getHubRouteId()),
                    companyRoute
            ));
        }

        return routeHistories;
    }

    public DeliveryRouteHistory toDeliveryRouteHistory(
            Delivery delivery,
            UUID companyDeliveryManagerId,
            UUID hubDeliveryManagerId,
            boolean companyRoute
    ) {
        DeliveryRouteType deliveryRouteType = companyRoute
                ? DeliveryRouteType.HUB_TO_COMPANY
                : DeliveryRouteType.HUB_TO_HUB;
        UUID deliveryManagerId = companyRoute
                ? companyDeliveryManagerId
                : hubDeliveryManagerId;
        DeliveryLocationType arrivalLocationType = companyRoute
                ? DeliveryLocationType.COMPANY
                : DeliveryLocationType.HUB;
        UUID arrivalId = companyRoute ? arrivalCompanyId : arrivalHubId;

        return DeliveryRouteHistory.create(
                delivery,
                deliveryManagerId,
                sequence,
                deliveryRouteType,
                DeliveryLocationType.HUB,
                departureHubId,
                arrivalLocationType,
                arrivalId,
                estimatedDistanceKm,
                estimatedDurationMin
        );
    }
}
