package com.msa.delivery_service.dto;

import com.msa.delivery_service.entity.Delivery;
import com.msa.delivery_service.entity.DeliveryRouteHistory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DeliveryDetailResponse {

    private DeliveryResponse delivery;
    private List<DeliveryRouteHistoryResponse> routeHistories;

    public static DeliveryDetailResponse of(Delivery delivery, List<DeliveryRouteHistory> routeHistories) {
        return DeliveryDetailResponse.builder()
                .delivery(DeliveryResponse.from(delivery))
                .routeHistories(routeHistories == null
                        ? List.of()
                        : routeHistories.stream()
                                .map(DeliveryRouteHistoryResponse::from)
                                .toList())
                .build();
    }
}
