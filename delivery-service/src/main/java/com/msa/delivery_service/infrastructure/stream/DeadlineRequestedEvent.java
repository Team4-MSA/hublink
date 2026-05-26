package com.msa.delivery_service.infrastructure.stream;

import com.msa.delivery_service.domain.entity.Delivery;
import com.msa.delivery_service.infrastructure.client.hub.dto.HubRouteResponse;
import com.msa.delivery_service.infrastructure.client.user.dto.DeliveryManagerResponse;
import com.msa.delivery_service.infrastructure.client.user.dto.HubManagerResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class DeadlineRequestedEvent {

    private UUID eventId;
    private UUID deliveryId;
    private UUID orderId;
    private String ordererName;
    private String ordererEmail;
    private LocalDateTime orderedAt;
    private String requestMessage;
    private UUID receiverUserId;
    private String receiverSlackId;
    private List<DeliveryRequest.Product> products;
    private LocalDateTime requestedArrivalAt;
    private String departureHubName;
    private String destinationAddress;
    private String deliveryManagerName;
    private String deliveryManagerEmail;
    private List<HubRouteResponse> routeInfo;
    private String workStartTime;
    private String workEndTime;

    public static DeadlineRequestedEvent of(
            Delivery delivery,
            DeliveryRequest request,
            HubManagerResponse hubManager,
            DeliveryManagerResponse deliveryManager,
            List<HubRouteResponse> hubRoutes,
            String workStartTime,
            String workEndTime
    ) {
        return DeadlineRequestedEvent.builder()
                .eventId(UUID.randomUUID())
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .ordererName(request.getOrdererName())
                .ordererEmail(request.getOrdererEmail())
                .orderedAt(request.getOrderedAt())
                .requestMessage(request.getRequestMessage())
                .receiverUserId(hubManager.getHubManagerId())
                .receiverSlackId(hubManager.getHubManagerSlackId())
                .products(request.getProducts())
                .requestedArrivalAt(request.getRequestedArrivalAt())
                .departureHubName(hubRoutes.get(0).getDepartureHubName())
                .destinationAddress(delivery.getDeliveryAddress())
                .deliveryManagerName(deliveryManager.getDeliveryManagerName())
                .deliveryManagerEmail(deliveryManager.getDeliveryManagerEmail())
                .routeInfo(hubRoutes)
                .workStartTime(workStartTime)
                .workEndTime(workEndTime)
                .build();
    }
}
